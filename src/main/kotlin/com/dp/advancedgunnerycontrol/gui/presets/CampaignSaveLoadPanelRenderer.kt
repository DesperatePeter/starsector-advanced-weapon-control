package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows

import com.dp.advancedgunnerycontrol.gui.*
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.style.*
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Save/Load preset action component.
 * Renders the per-group and all-groups Save/Load buttons. Availability and
 * execution live in focused preset helpers so modal behavior has one owner.
 */
object CampaignSaveLoadPanelRenderer {
    // The left-column panel reuses the per-group controls via this sentinel so
    // scope/backend/overwrite behavior cannot drift into a second save system.
    const val ALL_WEAPON_GROUPS_INDEX = -1

    private const val SAVE_LOAD_BUTTON_HEIGHT = 18f
    private const val SAVE_LOAD_BUTTON_HGAP = 1f

    const val PANEL_HEIGHT =
        SAVE_LOAD_BUTTON_HEIGHT
    const val COMPACT_PANEL_HEIGHT = SAVE_LOAD_BUTTON_HEIGHT

    fun renderActionButtonsOnly(
        panel: CustomPanelAPI,
        groupIndex: Int,
        top: Float,
        width: Float,
        state: PresetControlState,
        onStateChanged: (PresetControlState) -> Unit,
        onRefreshRequested: () -> Unit,
    ): List<ButtonBase<*>> {
        val effectiveState = normalizedState(state)
        if (effectiveState != state) {
            onStateChanged(effectiveState)
        }
        return mutableListOf<ButtonBase<*>>().apply {
            addPresetActionButtons(
                panel = panel,
                groupIndex = groupIndex,
                top = top,
                width = width,
                state = effectiveState,
                onStateChanged = onStateChanged,
                onRefreshRequested = onRefreshRequested,
            )
        }
    }

    fun allGroupsOptionRows(
        ship: FleetMemberAPI,
        state: PresetControlState,
        skipAvailabilityChecks: Boolean,
        onStateChanged: (PresetControlState) -> Unit,
        onRefreshRequested: () -> Unit,
    ): List<CampaignOptionRow> {
        return listOf(
            allGroupsOptionRow(
                ship,
                PendingPresetAction.SAVE,
                state,
                skipAvailabilityChecks,
                onStateChanged,
                onRefreshRequested,
            ),
            allGroupsOptionRow(
                ship,
                PendingPresetAction.LOAD,
                state,
                skipAvailabilityChecks,
                onStateChanged,
                onRefreshRequested,
            ),
        )
    }

    private fun allGroupsOptionRow(
        ship: FleetMemberAPI,
        action: PendingPresetAction,
        state: PresetControlState,
        skipAvailabilityChecks: Boolean,
        onStateChanged: (PresetControlState) -> Unit,
        onRefreshRequested: () -> Unit,
    ): CampaignOptionRow {
        val enabled = skipAvailabilityChecks ||
            PresetControlAvailabilityResolver.nonEmptyWeaponGroupIndexes(ship).isNotEmpty()
        val disabledTooltip = "There are no non-empty weapon groups to ${action.label().lowercase()}."
        return CampaignOptionRow(
            label = action.label(),
            tooltip = if (enabled) action.allGroupsTooltip() else disabledTooltip,
            kind = action.kind(),
            textColor = if (enabled) CampaignGuiStyle.DEFAULT_TEXT_COLOUR else CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR,
            rebuildAfter = false,
            callback = {
                if (enabled) {
                    onStateChanged(state.copy(pendingAction = action))
                    onRefreshRequested()
                }
            }
        )
    }

    private fun MutableList<ButtonBase<*>>.addPresetActionButtons(
        panel: CustomPanelAPI,
        groupIndex: Int,
        top: Float,
        width: Float,
        state: PresetControlState,
        onStateChanged: (PresetControlState) -> Unit,
        onRefreshRequested: () -> Unit,
    ) {
        val buttonWidth = (width - SAVE_LOAD_BUTTON_HGAP) / 2f

        addPresetActionButton(
            panel = panel,
            groupIndex = groupIndex,
            top = top,
            buttonWidth = buttonWidth,
            action = PendingPresetAction.SAVE,
            state = state,
            onStateChanged = onStateChanged,
            onRefreshRequested = onRefreshRequested,
        )
        addPresetActionButton(
            panel = panel,
            groupIndex = groupIndex,
            top = top,
            buttonWidth = buttonWidth,
            action = PendingPresetAction.LOAD,
            state = state,
            onStateChanged = onStateChanged,
            onRefreshRequested = onRefreshRequested,
        )
    }

    fun normalizedState(state: PresetControlState): PresetControlState {
        return PresetControlAvailabilityResolver.normalizedState(state)
    }

    private fun MutableList<ButtonBase<*>>.addPresetActionButton(
        panel: CustomPanelAPI,
        groupIndex: Int,
        top: Float,
        buttonWidth: Float,
        action: PendingPresetAction,
        state: PresetControlState,
        onStateChanged: (PresetControlState) -> Unit,
        onRefreshRequested: () -> Unit,
    ) {
        addPresetControlButton(
            panel = panel,
            groupIndex = groupIndex,
            top = top,
            buttonWidth = buttonWidth,
            height = SAVE_LOAD_BUTTON_HEIGHT,
            slot = action.slot(),
            dataPrefix = action.dataPrefix(),
            label = action.label(),
            kind = action.kind(),
            tooltip = action.tooltip(),
        ) {
            onStateChanged(state.copy(pendingAction = action))
            onRefreshRequested()
        }
    }

    fun canOpenPresetAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        action: PendingPresetAction,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean {
        return PresetControlAvailabilityResolver.canOpenPresetAction(
            ship,
            groupIndex,
            action,
            state,
            presetPeekCache,
        )
    }

    fun presetActionDisabledTooltip(
        ship: FleetMemberAPI,
        groupIndex: Int,
        action: PendingPresetAction,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): String? {
        return PresetControlAvailabilityResolver.presetActionDisabledTooltip(
            ship,
            groupIndex,
            action,
            state,
            presetPeekCache,
        )
    }

    fun selectableBackends(scope: WeaponPresetScope): List<WeaponPresetBackend> {
        return PresetControlAvailabilityResolver.selectableBackends(scope)
    }

    fun canToggleBackend(state: PresetControlState): Boolean {
        return PresetControlAvailabilityResolver.canToggleBackend(state)
    }

    fun canToggleOverwrite(state: PresetControlState): Boolean {
        return PresetControlAvailabilityResolver.canToggleOverwrite(state)
    }

    fun canExecutePendingAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        presetPeekCache: PresetPeekCache? = null,
    ): Boolean {
        return PresetControlAvailabilityResolver.canExecutePendingAction(ship, groupIndex, state, presetPeekCache)
    }

    fun scopeControlLabel(scope: WeaponPresetScope): String {
        return PresetControlAvailabilityResolver.scopeControlLabel(scope)
    }

    fun nextAvailableScope(scope: WeaponPresetScope): WeaponPresetScope {
        return PresetControlAvailabilityResolver.nextAvailableScope(scope)
    }

    fun previousAvailableScope(scope: WeaponPresetScope): WeaponPresetScope {
        return PresetControlAvailabilityResolver.previousAvailableScope(scope)
    }

    private fun buttonX(slot: Int, buttonWidth: Float): Float {
        return CampaignGuiStyle.PANEL_PADDING + slot * (buttonWidth + SAVE_LOAD_BUTTON_HGAP)
    }

    private fun MutableList<ButtonBase<*>>.addPresetControlButton(
        panel: CustomPanelAPI,
        groupIndex: Int,
        top: Float,
        buttonWidth: Float,
        height: Float,
        slot: Int,
        dataPrefix: String,
        label: String,
        kind: CampaignActionButtonKind,
        tooltip: String? = null,
        disabledTooltip: String? = null,
        centeredLabel: Boolean = true,
        enabled: Boolean = true,
        callback: () -> Unit,
    ): ButtonBase<*> {
        val template = CampaignGuiStyle.actionButtonTemplate(kind, enabled)
        val button = CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = panel,
            data = "${dataPrefix}_$groupIndex",
            x = buttonX(slot, buttonWidth),
            y = top,
            width = buttonWidth,
            height = height,
            tooltip = if (enabled) tooltip else disabledTooltip,
            template = template,
            labelText = label,
            showTooltipWhileInactive = !enabled && !disabledTooltip.isNullOrBlank(),
            centerConfirmCancelText = centeredLabel,
            centerText = centeredLabel,
            textPadding = CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
        ) { callback() }
        add(button)
        return button
    }

    fun requiresOverwriteWarning(state: PresetControlState): Boolean {
        return PresetActionExecutor.requiresOverwriteWarning(state)
    }

    fun requiresConfirmation(state: PresetControlState): Boolean {
        return PresetActionExecutor.requiresConfirmation(state)
    }

    fun executePendingAction(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        runtimeShip: ShipAPI?,
        presetPeekCache: PresetPeekCache? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): PresetActionExecutionResult {
        return PresetActionExecutor.executePendingAction(
            ship,
            groupIndex,
            state,
            runtimeShip,
            presetPeekCache,
            persistenceContext,
        )
    }
}
