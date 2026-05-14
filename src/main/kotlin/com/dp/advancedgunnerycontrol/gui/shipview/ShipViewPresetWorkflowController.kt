package com.dp.advancedgunnerycontrol.gui.shipview

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.input.ShipViewInputController
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationModalRequest
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.panels.weapongroups.WeaponGroupPanelRenderer
import com.dp.advancedgunnerycontrol.gui.presets.CampaignSaveLoadPanelRenderer
import com.dp.advancedgunnerycontrol.gui.presets.PendingPresetAction
import com.dp.advancedgunnerycontrol.gui.presets.PresetActionExecutionResult
import com.dp.advancedgunnerycontrol.gui.presets.PresetActionReviewBody
import com.dp.advancedgunnerycontrol.gui.presets.PresetConfirmationCopy
import com.dp.advancedgunnerycontrol.gui.presets.PresetControlState
import com.dp.advancedgunnerycontrol.gui.presets.ShipViewPresetActionModalController
import com.dp.advancedgunnerycontrol.gui.presets.ShipViewPresetStateController
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

internal class ShipViewPresetWorkflowController(
    initialStates: Map<Int, PresetControlState>,
    runtimeShip: ShipAPI?,
    onStateUpdate: ((Int, PresetControlState) -> Unit)?,
    buttons: MutableList<ButtonBase<*>>,
    private val activeShip: () -> FleetMemberAPI?,
    private val activePersistenceContext: () -> ShipEditorPersistenceContext?,
    private val rootPanel: () -> CustomPanelAPI?,
    private val loadoutIndex: () -> Int,
    suppressNonModalButtonHover: (Int) -> Unit,
    restoreSuppressedNonModalButtonHover: () -> Unit,
    setConfirmationDialogPosition: (PositionAPI?) -> Unit,
    private val confirmationDialogPosition: () -> PositionAPI?,
    private val markDirty: () -> Unit,
    private val refreshWeaponGroup: (Int) -> Unit,
    private val handleGroupTagsChanged: (Int, List<String>) -> Unit,
) {
    private val stateController = ShipViewPresetStateController(
        initialStates = initialStates,
        runtimeShip = runtimeShip,
        onStateUpdate = onStateUpdate,
    )
    private val modalController = ShipViewPresetActionModalController(
        buttons = buttons,
        suppressNonModalButtonHover = suppressNonModalButtonHover,
        restoreSuppressedNonModalButtonHover = restoreSuppressedNonModalButtonHover,
        setConfirmationDialogPosition = setConfirmationDialogPosition,
        markDirty = markDirty,
    )
    private val runtimeShip = runtimeShip

    fun snapshot(): Map<Int, PresetControlState> = stateController.snapshot()

    fun stateForGroup(groupIndex: Int): PresetControlState = stateController.stateForGroup(groupIndex)

    fun pendingConfirmation(): Pair<Int, PresetControlState>? = stateController.pendingConfirmation()

    fun pendingConfirmationRequest(): CampaignConfirmationModalRequest? {
        return pendingConfirmation()?.let { (groupIndex, state) ->
            confirmationRequest(groupIndex, state)
        }
    }

    fun allGroupsOptionRows(): List<CampaignOptionRow> {
        val ship = activeShip() ?: return emptyList()
        val groupIndex = CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX
        val state = stateForGroup(groupIndex)
        return CampaignSaveLoadPanelRenderer.allGroupsOptionRows(
            ship = ship,
            state = state,
            skipAvailabilityChecks = pendingConfirmation() != null,
            onStateChanged = { nextState -> update(groupIndex, nextState) },
            onRefreshRequested = { openModal(groupIndex, stateForGroup(groupIndex)) },
        )
    }

    fun markAllVisibleGroupsCleanToCurrentTags() {
        val ship = activeShip() ?: return
        stateController.markAllVisibleGroupsCleanToCurrentTags(
            ship,
            loadoutIndex(),
            activePersistenceContext(),
        )
    }

    fun update(groupIndex: Int, state: PresetControlState) {
        stateController.update(groupIndex, state, activeShip())
    }

    fun restorePersistentStates(ship: FleetMemberAPI) {
        stateController.restorePersistentStates(ship)
    }

    fun clearDirtyCache() {
        stateController.clearDirtyCache()
    }

    fun resetModal() {
        modalController.reset()
    }

    fun isDirtyForGroup(ship: FleetMemberAPI, groupIndex: Int, currentTags: List<String>? = null): Boolean {
        return stateController.isDirtyForGroup(
            ship = ship,
            groupIndex = groupIndex,
            loadoutIndex = loadoutIndex(),
            activeContext = activePersistenceContext(),
            pendingConfirmation = pendingConfirmation() != null,
            currentTags = currentTags,
        )
    }

    fun currentSanitizedTagsForGroup(ship: FleetMemberAPI, groupIndex: Int): List<String> {
        return stateController.currentSanitizedTagsForGroup(
            ship,
            groupIndex,
            loadoutIndex(),
            activePersistenceContext(),
        )
    }

    fun renderPendingModal(panel: CustomPanelAPI): Boolean {
        val (groupIndex, state) = pendingConfirmation() ?: return false
        renderModal(panel, groupIndex, state)
        return true
    }

    fun openModal(groupIndex: Int, state: PresetControlState) {
        val normalized = CampaignSaveLoadPanelRenderer.normalizedState(state)
        update(groupIndex, normalized)
        val panel = rootPanel() ?: run {
            markDirty()
            return
        }
        if (modalController.hasOpenModal()) {
            refreshModal(groupIndex, normalized)
        } else {
            renderModal(panel, groupIndex, normalized)
        }
    }

    fun processScopeWheelEvent(event: InputEventAPI): Boolean {
        return ShipViewInputController.processPresetScopeWheelEvent(
            event = event,
            pendingPreset = pendingConfirmation(),
            region = modalController.scopeWheelRegion,
            dialogPosition = confirmationDialogPosition(),
            updateAndRefresh = ::updateAndRefreshModal,
        )
    }

    fun canConfirm(groupIndex: Int, state: PresetControlState): Boolean {
        val ship = activeShip() ?: return false
        return CampaignSaveLoadPanelRenderer.canExecutePendingAction(ship, groupIndex, state, stateController.peekCache)
    }

    private fun renderModal(panel: CustomPanelAPI, groupIndex: Int, state: PresetControlState) {
        modalController.render(
            panel = panel,
            state = state,
            request = confirmationRequest(groupIndex, state),
            reviewBody = reviewBody(groupIndex, state),
            updateAndRefresh = { updatedState -> updateAndRefreshModal(groupIndex, updatedState) },
            canConfirm = { canConfirm(groupIndex, state) },
        )
    }

    private fun refreshModal(groupIndex: Int, state: PresetControlState) {
        modalController.refresh(
            state = state,
            request = confirmationRequest(groupIndex, state),
            reviewBody = reviewBody(groupIndex, state),
            updateAndRefresh = { updatedState -> updateAndRefreshModal(groupIndex, updatedState) },
            canConfirm = { canConfirm(groupIndex, state) },
        )
    }

    private fun updateAndRefreshModal(groupIndex: Int, state: PresetControlState) {
        val normalized = CampaignSaveLoadPanelRenderer.normalizedState(state)
        update(groupIndex, normalized)
        refreshModal(groupIndex, normalized)
    }

    private fun confirmationRequest(groupIndex: Int, state: PresetControlState): CampaignConfirmationModalRequest {
        val isOverwrite = CampaignSaveLoadPanelRenderer.requiresOverwriteWarning(state)
        val action = state.pendingAction ?: PendingPresetAction.SAVE
        val title = when {
            isOverwrite -> "Overwrite Save Warning"
            action == PendingPresetAction.SAVE -> "Confirm Save"
            else -> "Confirm Load"
        }
        return CampaignConfirmationModalRequest(
            title = title,
            body = "",
            richBody = PresetConfirmationCopy.body(
                groupIndex = groupIndex,
                state = state,
                ship = activeShip(),
                runtimeShip = runtimeShip,
                activePersistenceContext = activePersistenceContext(),
                loadoutIndex = loadoutIndex(),
                presetPeekCache = stateController.peekCache,
            ),
            tone = if (isOverwrite) CampaignConfirmationTone.WARNING else CampaignConfirmationTone.CAUTION,
            onConfirm = { confirmAction(groupIndex, state) },
            onCancel = { cancelAction(groupIndex, state) },
        )
    }

    private fun reviewBody(groupIndex: Int, state: PresetControlState): PresetActionReviewBody? {
        return PresetConfirmationCopy.reviewBody(
            groupIndex = groupIndex,
            state = state,
            ship = activeShip(),
            runtimeShip = runtimeShip,
            activePersistenceContext = activePersistenceContext(),
            loadoutIndex = loadoutIndex(),
            presetPeekCache = stateController.peekCache,
        )
    }

    private fun confirmAction(groupIndex: Int, state: PresetControlState) {
        val ship = activeShip() ?: return
        val result = CampaignSaveLoadPanelRenderer.executePendingAction(
            ship,
            groupIndex,
            state,
            runtimeShip,
            stateController.peekCache,
            activePersistenceContext(),
        )
        stateController.clearPeekCache()
        if (result.executed) {
            updateCleanPresetBaseline(ship, groupIndex, state)
            closeModalTargeted(groupIndex, state)
            refreshActionResult(ship, groupIndex, result)
        } else {
            update(groupIndex, state.copy(pendingAction = null))
            closeModalTargeted(groupIndex, state)
        }
    }

    private fun updateCleanPresetBaseline(ship: FleetMemberAPI, groupIndex: Int, state: PresetControlState) {
        stateController.updateCleanPresetBaseline(
            ship = ship,
            groupIndex = groupIndex,
            state = state,
            loadoutIndex = loadoutIndex(),
            activeContext = activePersistenceContext(),
        )
    }

    private fun cancelAction(groupIndex: Int, state: PresetControlState) {
        closeModalTargeted(groupIndex, state)
    }

    private fun closeModalTargeted(groupIndex: Int, state: PresetControlState) {
        update(groupIndex, state.copy(pendingAction = null, overwrite = false))
        modalController.closeTargeted()
    }

    private fun refreshActionResult(
        ship: FleetMemberAPI,
        groupIndex: Int,
        result: PresetActionExecutionResult,
    ) {
        val groups = if (groupIndex == CampaignSaveLoadPanelRenderer.ALL_WEAPON_GROUPS_INDEX) {
            affectedNonEmptyGroups(ship, result.affectedGroupIndexes)
        } else {
            result.affectedGroupIndexes.ifEmpty { setOf(groupIndex) }
        }
        when (result.action) {
            PendingPresetAction.LOAD -> groups.forEach { index ->
                refreshWeaponGroup(index)
                handleGroupTagsChanged(index, currentSanitizedTagsForGroup(ship, index))
            }
            PendingPresetAction.SAVE -> groups.forEach { index ->
                handleGroupTagsChanged(index, currentSanitizedTagsForGroup(ship, index))
            }
            null -> Unit
        }
    }

    private fun affectedNonEmptyGroups(ship: FleetMemberAPI, affectedGroups: Set<Int>): Set<Int> {
        return stateController.affectedNonEmptyGroups(ship, affectedGroups)
    }
}
