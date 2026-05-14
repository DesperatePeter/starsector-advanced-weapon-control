package com.dp.advancedgunnerycontrol.gui.suggestedtags.modals

import com.dp.advancedgunnerycontrol.gui.controls.confirmations.PendingConfirmationState


import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagDangerousAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.suggestedDangerousActionConfirmationRequest
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationModalRequest
import com.dp.advancedgunnerycontrol.gui.modals.RenderedCampaignConfirmationModal
import com.dp.advancedgunnerycontrol.gui.modals.processCampaignConfirmationModalInput
import com.dp.advancedgunnerycontrol.gui.modals.renderCampaignConfirmationModal
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

data class SuggestedConfirmationModalIds(
    val confirm: String,
    val cancel: String,
    val backdrop: String,
)

fun pendingSuggestedDangerousActionRequest(
    pendingAction: PendingConfirmationState<SuggestedTagDangerousAction>,
): CampaignConfirmationModalRequest? {
    val pending = pendingAction.key ?: return null
    return suggestedDangerousActionConfirmationRequest(pending, pendingAction::clear)
}

fun processSuggestedConfirmationModalInput(
    events: MutableList<InputEventAPI>?,
    requestProvider: () -> CampaignConfirmationModalRequest?,
    rebuild: () -> Unit,
    afterCancel: () -> Unit = rebuild,
    afterConfirm: () -> Unit = rebuild,
): Boolean {
    val request = requestProvider() ?: return false
    return processCampaignConfirmationModalInput(
        events = events,
        onCancel = {
            request.onCancel()
            afterCancel()
        },
        onConfirm = {
            request.onConfirm()
            afterConfirm()
        },
    )
}

/**
 * Suggested-tags warning popup component.
 * Used for Backup, Reset, and Restore confirmation in campaign and direct/refit
 * Customize Suggested Tags hosts.
 */
fun renderSuggestedConfirmationModal(
    root: CustomPanelAPI,
    request: CampaignConfirmationModalRequest?,
    ids: SuggestedConfirmationModalIds,
    suppressNonModalButtonHover: () -> Unit,
    registerModalButtons: (RenderedCampaignConfirmationModal, CampaignConfirmationModalRequest) -> Unit,
): RenderedCampaignConfirmationModal? {
    request ?: return null
    suppressNonModalButtonHover()
    val modal = renderCampaignConfirmationModal(
        root = root,
        request = request,
        confirmData = ids.confirm,
        cancelData = ids.cancel,
        backdropData = ids.backdrop
    )
    registerModalButtons(modal, request)
    return modal
}
