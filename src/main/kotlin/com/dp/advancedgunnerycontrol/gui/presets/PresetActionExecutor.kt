package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetLoadStatus
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetSaveStatus
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.dp.advancedgunnerycontrol.presets.loadWeaponPreset
import com.dp.advancedgunnerycontrol.presets.saveWeaponPreset
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetActionExecutor {
    private val log = Global.getLogger(PresetActionExecutor::class.java)

    fun requiresOverwriteWarning(state: PresetControlState): Boolean {
        val effectiveState = PresetControlAvailabilityResolver.normalizedState(state)
        return effectiveState.pendingAction == PendingPresetAction.SAVE &&
            effectiveState.overwrite &&
            effectiveState.scope != WeaponPresetScope.SUGGESTED
    }

    fun requiresConfirmation(state: PresetControlState): Boolean {
        return PresetControlAvailabilityResolver.normalizedState(state).pendingAction != null
    }

    fun executePendingAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        presetPeekCache: PresetPeekCache? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): PresetActionExecutionResult {
        val effectiveState = PresetControlAvailabilityResolver.normalizedState(state)
        val action = effectiveState.pendingAction
        if (
            action != null &&
            !PresetControlAvailabilityResolver.presetActionAvailability(
                ship,
                groupIndex,
                action,
                effectiveState,
                presetPeekCache,
            ).enabled
        ) {
            log.info("Ignored unavailable ${action.label()} preset action for scope=${effectiveState.scope}, backend=${effectiveState.backend}.")
            return PresetActionExecutionResult(executed = false, action = action)
        }
        return when (action) {
            PendingPresetAction.SAVE -> {
                val affected = executeSave(ship, groupIndex, effectiveState, runtimeShip, persistenceContext)
                PresetActionExecutionResult(
                    executed = affected.isNotEmpty(),
                    action = action,
                    affectedGroupIndexes = affected,
                )
            }
            PendingPresetAction.LOAD -> {
                val affected = executeLoad(ship, groupIndex, effectiveState, runtimeShip, persistenceContext)
                PresetActionExecutionResult(
                    executed = affected.isNotEmpty(),
                    action = action,
                    affectedGroupIndexes = affected,
                )
            }
            null -> PresetActionExecutionResult(executed = false, action = null)
        }
    }

    private fun executeSave(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
    ): Set<Int> {
        if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return executeSaveAllGroups(ship, state, runtimeShip, persistenceContext)
        }
        val result = saveWeaponPreset(
            ship,
            groupIndex,
            AGCGUI.storageIndex,
            state.scope,
            state.backend,
            state.overwrite,
            runtimeShip = runtimeShip,
            persistenceContext = persistenceContext,
        )
        when (result.status) {
            WeaponCompositionPresetSaveStatus.FAILED -> {
                log.warn("Failed to save weapon preset. See log.")
            }
            WeaponCompositionPresetSaveStatus.NO_WEAPON_GROUP_KEY -> {
                log.info("No weapons in this group.")
            }
            WeaponCompositionPresetSaveStatus.SUGGESTED_REQUIRES_SINGLE_WEAPON -> {
                log.info("Suggested presets can only be saved from groups with one weapon type.")
            }
            WeaponCompositionPresetSaveStatus.SAVED -> {
                log.info("Saved ${state.scope.label()} ${state.backend.label()} preset; overwritten groups=${result.overwrittenGroups}.")
            }
        }
        return if (result.status == WeaponCompositionPresetSaveStatus.SAVED) setOf(groupIndex) else emptySet()
    }

    private fun executeLoad(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
    ): Set<Int> {
        if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return executeLoadAllGroups(ship, state, runtimeShip, persistenceContext)
        }
        val result = loadWeaponPreset(
            ship,
            groupIndex,
            AGCGUI.storageIndex,
            state.scope,
            state.backend,
            runtimeShip = runtimeShip,
            persistenceContext = persistenceContext,
        )
        when (result.status) {
            WeaponCompositionPresetLoadStatus.FAILED -> {
                log.warn("Failed to load weapon preset. See log.")
            }
            WeaponCompositionPresetLoadStatus.NO_WEAPON_GROUP_KEY -> {
                log.info("No weapons in this group.")
            }
            WeaponCompositionPresetLoadStatus.NO_PRESET_FOUND -> {
                log.info("No ${state.scope.label()} ${state.backend.label()} preset saved for this weapon combination.")
            }
            WeaponCompositionPresetLoadStatus.LOADED -> {
                val imported = result.importedCustomTags.takeIf { it.isNotEmpty() }
                    ?.joinToString(prefix = "; imported custom tags=", separator = ", ")
                    ?: ""
                log.info("Loaded ${state.scope.label()} ${state.backend.label()} preset for this weapon combination$imported.")
            }
        }
        return if (result.status == WeaponCompositionPresetLoadStatus.LOADED) setOf(groupIndex) else emptySet()
    }

    private fun executeSaveAllGroups(
        ship: FleetMemberAPI,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
    ): Set<Int> {
        val groupIndexes = PresetControlAvailabilityResolver.nonEmptyWeaponGroupIndexes(ship)
        if (groupIndexes.isEmpty()) {
            log.info("No weapons in any group.")
            return emptySet()
        }
        val savedGroups = mutableSetOf<Int>()
        var saved = 0
        var skipped = 0
        var failed = 0
        var overwritten = 0
        groupIndexes.forEach { groupIndex ->
            val result = saveWeaponPreset(
                ship,
                groupIndex,
                AGCGUI.storageIndex,
                state.scope,
                state.backend,
                state.overwrite,
                runtimeShip = runtimeShip,
                persistenceContext = persistenceContext,
            )
            when (result.status) {
                WeaponCompositionPresetSaveStatus.SAVED -> {
                    saved++
                    savedGroups.add(groupIndex)
                    overwritten += result.overwrittenGroups
                }
                WeaponCompositionPresetSaveStatus.NO_WEAPON_GROUP_KEY,
                WeaponCompositionPresetSaveStatus.SUGGESTED_REQUIRES_SINGLE_WEAPON -> skipped++
                WeaponCompositionPresetSaveStatus.FAILED -> failed++
            }
        }
        log.info(
            "Saved all-groups ${state.scope.label()} ${state.backend.label()} presets; " +
                "saved=$saved skipped=$skipped failed=$failed overwrittenGroups=$overwritten."
        )
        return savedGroups
    }

    private fun executeLoadAllGroups(
        ship: FleetMemberAPI,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
    ): Set<Int> {
        val groupIndexes = PresetControlAvailabilityResolver.nonEmptyWeaponGroupIndexes(ship)
        if (groupIndexes.isEmpty()) {
            log.info("No weapons in any group.")
            return emptySet()
        }
        val loadedGroups = mutableSetOf<Int>()
        var loaded = 0
        var missing = 0
        var skipped = 0
        var failed = 0
        var importedCustomTags = 0
        groupIndexes.forEach { groupIndex ->
            val result = loadWeaponPreset(
                ship,
                groupIndex,
                AGCGUI.storageIndex,
                state.scope,
                state.backend,
                runtimeShip = runtimeShip,
                persistenceContext = persistenceContext,
            )
            when (result.status) {
                WeaponCompositionPresetLoadStatus.LOADED -> {
                    loaded++
                    loadedGroups.add(groupIndex)
                    importedCustomTags += result.importedCustomTags.size
                }
                WeaponCompositionPresetLoadStatus.NO_PRESET_FOUND -> missing++
                WeaponCompositionPresetLoadStatus.NO_WEAPON_GROUP_KEY -> skipped++
                WeaponCompositionPresetLoadStatus.FAILED -> failed++
            }
        }
        log.info(
            "Loaded all-groups ${state.scope.label()} ${state.backend.label()} presets; " +
                "loaded=$loaded missing=$missing skipped=$skipped failed=$failed importedCustomTags=$importedCustomTags."
        )
        return loadedGroups
    }
}
