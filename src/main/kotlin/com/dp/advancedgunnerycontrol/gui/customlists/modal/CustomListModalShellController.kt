package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

private object CustomListModalShellControllerLog

internal class CustomListModalShellController(
    private val buttons: MutableList<ButtonBase<*>>,
) {
    private var rootPanel: CustomPanelAPI? = null
    private var shell: ShipViewModalShell? = null
    private var buttonStartIndex: Int = -1
    private var buttonEndIndex: Int = -1
    var dialogPosition: PositionAPI? = null
        private set

    fun createShell(
        panel: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        firstModalButtonIndex: Int,
    ): ShipViewModalShell {
        val rendered = ShipViewModalFactory.createShell(
            panel = panel,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            borderColor = CampaignGuiStyle.SAVE_BUTTON_HOVER_COLOR,
        )
        addBackdropButtons(rendered.backdrop, rendered.bounds.x, rendered.bounds.y, rendered.bounds.width, rendered.bounds.height)
        rootPanel = panel
        shell = rendered
        buttonStartIndex = firstModalButtonIndex
        dialogPosition = rendered.dialog.position
        return rendered
    }

    fun finishRender() {
        buttonEndIndex = buttons.size
    }

    fun detachAndClear(): CustomPanelAPI? {
        val panel = rootPanel
        val rendered = shell
        try {
            if (panel != null && rendered != null) {
                runCatching {
                    detachShipViewModal(panel, rendered, buttons, buttonStartIndex, buttonEndIndex)
                }.onFailure { ex ->
                    Global.getLogger(CustomListModalShellControllerLog::class.java)
                        .warn("[AGC_CUSTOM_LIST_MODAL] Failed to detach custom-list modal", ex)
                }
                return panel
            }
            return null
        } finally {
            clearRefs()
        }
    }

    fun reset() {
        clearRefs()
    }

    fun addBackdropButton(
        backdrop: CustomPanelAPI,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ): ButtonAPI? {
        return addTransparentCampaignInputShield(
            parent = backdrop,
            data = "custom_tag_modal_backdrop",
            x = x,
            y = y,
            width = width,
            height = height,
        )
    }

    private fun addBackdropButtons(
        backdrop: CustomPanelAPI,
        dialogX: Float,
        dialogY: Float,
        dialogWidth: Float,
        dialogHeight: Float,
    ) {
        val rootWidth = backdrop.position.width
        val rootHeight = backdrop.position.height
        listOfNotNull(
            addBackdropButton(backdrop, 0f, 0f, rootWidth, dialogY),
            addBackdropButton(backdrop, 0f, dialogY + dialogHeight, rootWidth, rootHeight - dialogY - dialogHeight),
            addBackdropButton(backdrop, 0f, dialogY, dialogX, dialogHeight),
            addBackdropButton(backdrop, dialogX + dialogWidth, dialogY, rootWidth - dialogX - dialogWidth, dialogHeight),
        ).forEach { button ->
            CampaignButtonControls.addControl(buttons, button = button) {}
        }
    }

    private fun clearRefs() {
        rootPanel = null
        shell = null
        buttonStartIndex = -1
        buttonEndIndex = -1
        dialogPosition = null
    }
}
