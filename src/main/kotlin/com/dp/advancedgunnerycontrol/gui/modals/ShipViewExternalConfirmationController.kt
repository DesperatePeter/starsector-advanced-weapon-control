package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

internal class ShipViewExternalConfirmationController(
    private val buttons: MutableList<ButtonBase<*>>,
    private val suppressNonModalButtonHover: (Int) -> Unit,
    private val restoreSuppressedNonModalButtonHover: () -> Unit,
    private val setConfirmationDialogPosition: (PositionAPI?) -> Unit,
    private val markDirty: () -> Unit,
) {
    private val log = Global.getLogger(ShipViewExternalConfirmationController::class.java)
    private var rootPanel: CustomPanelAPI? = null
    private var modal: RenderedCampaignConfirmationModal? = null
    private var buttonStartIndex: Int = -1
    private var buttonEndIndex: Int = -1

    fun hasOpenModal(): Boolean = modal != null

    fun reset() {
        clearRefs()
    }

    fun render(panel: CustomPanelAPI, request: CampaignConfirmationModalRequest) {
        val firstModalButtonIndex = buttons.size
        suppressNonModalButtonHover(firstModalButtonIndex)
        val rendered = renderCampaignConfirmationModal(
            root = panel,
            request = request,
            confirmData = "confirmation_modal_confirm",
            cancelData = "confirmation_modal_cancel",
            backdropData = "confirmation_modal_backdrop",
        )
        rootPanel = panel
        modal = rendered
        buttonStartIndex = firstModalButtonIndex
        setConfirmationDialogPosition(rendered.dialogPosition)
        CampaignButtonControls.addRenderedConfirmationModalButtons(buttons,
            modal = rendered,
            onConfirm = request.onConfirm,
            onCancel = {
                request.onCancel()
                closeTargeted()
            },
        )
        buttonEndIndex = buttons.size
    }

    fun closeTargeted() {
        val panel = rootPanel
        val rendered = modal
        if (panel == null || rendered == null) {
            markDirty()
            setConfirmationDialogPosition(null)
            restoreSuppressedNonModalButtonHover()
            return
        }
        try {
            runCatching { panel.removeComponent(rendered.backdrop) }
                .onFailure { ex ->
                    log.warn("[AGC_SHIP_VIEW] Failed to remove external confirmation backdrop", ex)
                }
            runCatching { panel.removeComponent(rendered.dialog) }
                .onFailure { ex ->
                    log.warn("[AGC_SHIP_VIEW] Failed to remove external confirmation dialog", ex)
                }
            CampaignButtonControls.clearRenderedConfirmationModalButtons(buttons, buttonStartIndex, buttonEndIndex)
        } finally {
            clearRefs()
            setConfirmationDialogPosition(null)
            restoreSuppressedNonModalButtonHover()
        }
    }

    private fun clearRefs() {
        rootPanel = null
        modal = null
        buttonStartIndex = -1
        buttonEndIndex = -1
    }
}
