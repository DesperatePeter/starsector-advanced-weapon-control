package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetPeekStatus
import com.dp.advancedgunnerycontrol.shipdata.getVariantWeaponGroup
import com.dp.advancedgunnerycontrol.presets.sanitizeWeaponCompositionPresetTagsForGroup
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal class ShipViewPresetStateController(
    initialStates: Map<Int, PresetControlState>,
    private val runtimeShip: ShipAPI?,
    private val onStateUpdate: ((Int, PresetControlState) -> Unit)?,
) {
    private val statesByGroup = initialStates.toMutableMap()
    private val dirtyByGroup = mutableMapOf<Int, Boolean>()
    val peekCache = PresetPeekCache()

    fun snapshot(): Map<Int, PresetControlState> = statesByGroup.toMap()

    fun stateForGroup(groupIndex: Int): PresetControlState {
        return statesByGroup[groupIndex] ?: PresetControlState()
    }

    fun pendingConfirmation(): Pair<Int, PresetControlState>? {
        return statesByGroup.entries.firstOrNull { (_, state) ->
            CampaignSaveLoadPanelRenderer.requiresConfirmation(state)
        }?.let { it.key to CampaignSaveLoadPanelRenderer.normalizedState(it.value) }
    }

    fun update(
        groupIndex: Int,
        state: PresetControlState,
        activeShip: FleetMemberAPI?,
    ) {
        val effectiveState = mergePresetActionWithRememberedSettings(groupIndex, state, activeShip)
        if (effectiveState == PresetControlState()) {
            statesByGroup.remove(groupIndex)
        } else {
            statesByGroup[groupIndex] = effectiveState
        }
        activeShip?.let { ship ->
            PresetControlStateMemory.remember(ship, groupIndex, effectiveState)
        }
        onStateUpdate?.invoke(groupIndex, effectiveState)
    }

    fun restorePersistentStates(ship: FleetMemberAPI) {
        PresetControlStateMemory.statesFor(ship).forEach { (groupIndex, storedState) ->
            if (groupIndex !in statesByGroup) {
                statesByGroup[groupIndex] = storedState
            }
        }
    }

    fun isDirtyForGroup(
        ship: FleetMemberAPI,
        groupIndex: Int,
        loadoutIndex: Int,
        activeContext: ShipEditorPersistenceContext?,
        pendingConfirmation: Boolean,
        currentTags: List<String>? = null,
    ): Boolean {
        if (pendingConfirmation) {
            return dirtyByGroup[groupIndex] ?: false
        }
        return if (currentTags == null) {
            dirtyByGroup.getOrPut(groupIndex) {
                computeDirtyForGroup(ship, groupIndex, loadoutIndex, activeContext, currentTags = null)
            }
        } else {
            computeDirtyForGroup(ship, groupIndex, loadoutIndex, activeContext, currentTags).also { isDirty ->
                dirtyByGroup[groupIndex] = isDirty
            }
        }
    }

    fun currentSanitizedTagsForGroup(
        ship: FleetMemberAPI,
        groupIndex: Int,
        loadoutIndex: Int,
        activeContext: ShipEditorPersistenceContext?,
    ): List<String> {
        val context = activeContext ?: ShipEditorPersistenceContext(ship, runtimeShip)
        return sanitizeWeaponCompositionPresetTagsForGroup(
            ship,
            groupIndex,
            context.loadWeaponTags(groupIndex, loadoutIndex),
        )
    }

    fun markAllVisibleGroupsCleanToCurrentTags(
        ship: FleetMemberAPI,
        loadoutIndex: Int,
        activeContext: ShipEditorPersistenceContext?,
    ) {
        visibleWeaponGroupIndexes(ship).forEach { index ->
            val current = stateForGroup(index)
            val cleanTags = currentSanitizedTagsForGroup(ship, index, loadoutIndex, activeContext)
            PresetCleanBaselineStore.put(ship, index, loadoutIndex, cleanTags)
            update(index, current.copy(cleanTags = cleanTags), ship)
        }
        dirtyByGroup.clear()
    }

    fun updateCleanPresetBaseline(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        loadoutIndex: Int,
        activeContext: ShipEditorPersistenceContext?,
    ) {
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            val cleanTags = currentSanitizedTagsForGroup(ship, groupIndex, loadoutIndex, activeContext)
            PresetCleanBaselineStore.put(ship, groupIndex, loadoutIndex, cleanTags)
            update(
                groupIndex,
                state.copy(pendingAction = null, overwrite = false, cleanTags = cleanTags),
                ship,
            )
            return
        }
        update(groupIndex, state.copy(pendingAction = null, overwrite = false), ship)
        visibleWeaponGroupIndexes(ship).forEach { index ->
            val current = stateForGroup(index)
            val cleanTags = currentSanitizedTagsForGroup(ship, index, loadoutIndex, activeContext)
            PresetCleanBaselineStore.put(ship, index, loadoutIndex, cleanTags)
            update(index, current.copy(cleanTags = cleanTags), ship)
        }
    }

    fun affectedNonEmptyGroups(ship: FleetMemberAPI, affectedGroups: Set<Int>): Set<Int> {
        return affectedGroups.ifEmpty {
            visibleWeaponGroupIndexes(ship).toSet()
        }
    }

    fun clearPeekCache() {
        peekCache.clear()
    }

    fun clearDirtyCache() {
        dirtyByGroup.clear()
    }

    private fun mergePresetActionWithRememberedSettings(
        groupIndex: Int,
        state: PresetControlState,
        activeShip: FleetMemberAPI?,
    ): PresetControlState {
        val action = state.pendingAction ?: return state
        val existing = statesByGroup[groupIndex]
            ?: activeShip?.let { PresetControlStateMemory.stateFor(it, groupIndex) }
            ?: return state
        val actionOnly = state.copy(pendingAction = null) == PresetControlState()
        return if (actionOnly) {
            existing.copy(pendingAction = action)
        } else {
            state
        }
    }

    private fun computeDirtyForGroup(
        ship: FleetMemberAPI,
        groupIndex: Int,
        loadoutIndex: Int,
        activeContext: ShipEditorPersistenceContext?,
        currentTags: List<String>?,
    ): Boolean {
        val currentSanitized = if (currentTags == null) {
            currentSanitizedTagsForGroup(ship, groupIndex, loadoutIndex, activeContext)
        } else {
            sanitizeWeaponCompositionPresetTagsForGroup(ship, groupIndex, currentTags)
        }.toSet()
        val presetState = stateForGroup(groupIndex)
        (presetState.cleanTags ?: PresetCleanBaselineStore.get(ship, groupIndex, loadoutIndex))
            ?.let { return currentSanitized != it.toSet() }
        val presetResult = peekCache.peek(
            member = ship,
            groupIndex = groupIndex,
            loadoutIndex = loadoutIndex,
            scope = presetState.scope,
            backend = presetState.backend,
        )
        return when (presetResult.status) {
            WeaponCompositionPresetPeekStatus.FOUND -> {
                PresetCleanBaselineStore.put(ship, groupIndex, loadoutIndex, presetResult.tags)
                currentSanitized != presetResult.tags.toSet()
            }
            WeaponCompositionPresetPeekStatus.NO_PRESET_FOUND,
            WeaponCompositionPresetPeekStatus.NO_WEAPON_GROUP_KEY -> {
                PresetCleanBaselineStore.put(ship, groupIndex, loadoutIndex, emptyList())
                currentSanitized.isNotEmpty()
            }
            WeaponCompositionPresetPeekStatus.FAILED -> true
        }
    }

    private fun visibleWeaponGroupIndexes(ship: FleetMemberAPI): List<Int> {
        return (0 until Values.MAX_WEAPON_GROUPS)
            .filter { index -> getVariantWeaponGroup(ship, index)?.slots?.isNotEmpty() == true }
    }
}
