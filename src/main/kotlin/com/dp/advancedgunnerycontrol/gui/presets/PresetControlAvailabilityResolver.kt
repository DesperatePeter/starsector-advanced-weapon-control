package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetPeekStatus
import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.dp.advancedgunnerycontrol.shipdata.getVariantWeaponGroup
import com.dp.advancedgunnerycontrol.presets.getWeaponCompositionPresetKey
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetControlAvailabilityResolver {
    fun normalizedState(state: PresetControlState): PresetControlState {
        return state.normalized()
    }

    private fun PresetControlState.normalized(): PresetControlState {
        var normalized = this
        if (!Settings.isAdvancedMode) {
            normalized = normalized.copy(
                scope = if (normalized.scope in simpleModeScopes()) normalized.scope else WeaponPresetScope.SINGLE,
                backend = WeaponPresetBackend.CAMPAIGN,
                overwrite = false,
            )
        }
        if (normalized.scope == WeaponPresetScope.SINGLE && normalized.backend == WeaponPresetBackend.EXTERNAL) {
            normalized = normalized.copy(backend = WeaponPresetBackend.CAMPAIGN)
        }
        if (normalized.scope == WeaponPresetScope.SUGGESTED && normalized.overwrite) {
            normalized = normalized.copy(overwrite = false)
        }
        return normalized
    }

    fun presetActionAvailability(
        ship: FleetMemberAPI,
        groupIndex: Int,
        action: PendingPresetAction,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): PresetControlAvailability {
        val nonEmptyGroups = nonEmptyWeaponGroupIndexes(ship)
        if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX && nonEmptyGroups.isEmpty()) {
            return disabled("There are no non-empty weapon groups to ${action.label().lowercase()}.")
        }
        return when (action) {
            PendingPresetAction.SAVE -> saveActionAvailability(ship, groupIndex, state)
            PendingPresetAction.LOAD -> loadActionAvailability(ship, groupIndex, state, presetPeekCache)
        }
    }

    fun canOpenPresetAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        action: PendingPresetAction,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean = presetActionAvailability(ship, groupIndex, action, state.normalized(), presetPeekCache).enabled

    fun presetActionDisabledTooltip(
        ship: FleetMemberAPI,
        groupIndex: Int,
        action: PendingPresetAction,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): String? = presetActionAvailability(ship, groupIndex, action, state.normalized(), presetPeekCache).disabledTooltip

    private fun saveActionAvailability(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
    ): PresetControlAvailability {
        if (state.scope != WeaponPresetScope.SUGGESTED) {
            return enabled()
        }
        if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return if (suggestedSaveEligibleGroupIndexes(ship).isNotEmpty()) {
                enabled()
            } else {
                disabled(
                    "Suggested presets are saved per weapon type, and this ship has no single-weapon groups to save."
                )
            }
        }
        return if (isSuggestedSaveEligibleGroup(ship, groupIndex)) {
            enabled()
        } else {
            disabled(
                "Suggested presets are saved per weapon type. This group has multiple weapon types, so it cannot be saved as a Suggested preset."
            )
        }
    }

    private fun loadActionAvailability(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): PresetControlAvailability {
        if (
            state.pendingAction != PendingPresetAction.LOAD &&
            loadPresetExistsForAnySelectableBackend(ship, groupIndex, state, presetPeekCache)
        ) {
            return enabled()
        }
        return if (loadPresetExists(ship, groupIndex, state, presetPeekCache)) {
            enabled()
        } else {
            disabled(noPresetTooltip(ship, groupIndex, state))
        }
    }

    private fun enabled(): PresetControlAvailability = PresetControlAvailability(enabled = true)

    private fun disabled(tooltip: String): PresetControlAvailability {
        return PresetControlAvailability(enabled = false, disabledTooltip = tooltip)
    }

    private fun loadPresetExists(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean {
        if (groupIndex != CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            return PresetPeekCache.peek(
                presetPeekCache,
                ship,
                groupIndex,
                AGCGUI.storageIndex,
                state.scope,
                state.backend,
            )
                .status == WeaponCompositionPresetPeekStatus.FOUND
        }
        return nonEmptyWeaponGroupIndexes(ship).any { index ->
            PresetPeekCache.peek(
                presetPeekCache,
                ship,
                index,
                AGCGUI.storageIndex,
                state.scope,
                state.backend,
            )
                .status == WeaponCompositionPresetPeekStatus.FOUND
        }
    }

    private fun loadPresetExistsForAnySelectableBackend(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean {
        return selectableBackends(state.scope).any { backend ->
            loadPresetExists(ship, groupIndex, state.copy(backend = backend), presetPeekCache)
        }
    }

    fun selectableBackends(scope: WeaponPresetScope): List<WeaponPresetBackend> {
        return if (Settings.isAdvancedMode && scope != WeaponPresetScope.SINGLE) {
            listOf(WeaponPresetBackend.CAMPAIGN, WeaponPresetBackend.EXTERNAL)
        } else {
            listOf(WeaponPresetBackend.CAMPAIGN)
        }
    }

    fun canToggleBackend(state: PresetControlState): Boolean = selectableBackends(state.scope).size > 1

    fun canToggleOverwrite(state: PresetControlState): Boolean = overwriteAvailability(state).enabled

    fun canExecutePendingAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean {
        val effectiveState = state.normalized()
        val action = effectiveState.pendingAction ?: return false
        return presetActionAvailability(ship, groupIndex, action, effectiveState, presetPeekCache).enabled
    }

    private fun noPresetTooltip(ship: FleetMemberAPI, groupIndex: Int, state: PresetControlState): String {
        return CampaignPresetTerminology.noPresetTooltip(
            ship,
            groupIndex,
            state,
            CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX,
        )
    }

    private fun isSuggestedSaveEligibleGroup(ship: FleetMemberAPI, groupIndex: Int): Boolean {
        val key = getWeaponCompositionPresetKey(ship, groupIndex)
            ?: return false
        return key.split("|").size == 1
    }

    private fun suggestedSaveEligibleGroupIndexes(ship: FleetMemberAPI): List<Int> {
        return nonEmptyWeaponGroupIndexes(ship).filter { index ->
            isSuggestedSaveEligibleGroup(ship, index)
        }
    }

    private fun overwriteAvailability(state: PresetControlState): PresetControlAvailability {
        if (!Settings.isAdvancedMode) {
            return disabled("Overwrite is only available in advanced mode.")
        }
        return if (state.scope == WeaponPresetScope.SUGGESTED) {
            disabled("Overwrite does not apply to Suggested presets because they are weapon-type recommendations, not ship/class/global active presets.")
        } else {
            enabled()
        }
    }

    fun scopeControlLabel(scope: WeaponPresetScope): String {
        val scopes = availableScopes()
        val index = scopes.indexOf(scope).coerceAtLeast(0)
        return "${scope.label()} [${index + 1}/${scopes.size}]"
    }

    fun nextAvailableScope(scope: WeaponPresetScope): WeaponPresetScope {
        return adjacentAvailableScope(scope, 1)
    }

    fun previousAvailableScope(scope: WeaponPresetScope): WeaponPresetScope {
        return adjacentAvailableScope(scope, -1)
    }

    private fun adjacentAvailableScope(scope: WeaponPresetScope, delta: Int): WeaponPresetScope {
        val scopes = availableScopes()
        val index = scopes.indexOf(scope).takeIf { it >= 0 } ?: 0
        return scopes[(index + delta + scopes.size) % scopes.size]
    }

    private fun availableScopes(): List<WeaponPresetScope> {
        return if (Settings.isAdvancedMode) {
            WeaponPresetScope.values().toList()
        } else {
            simpleModeScopes()
        }
    }

    private fun simpleModeScopes(): List<WeaponPresetScope> {
        return listOf(WeaponPresetScope.SINGLE, WeaponPresetScope.CLASS)
    }

    fun nonEmptyWeaponGroupIndexes(ship: FleetMemberAPI): List<Int> {
        return (0 until Values.MAX_WEAPON_GROUPS)
            .filter { index -> getVariantWeaponGroup(ship, index)?.slots?.isNotEmpty() == true }
    }
}
