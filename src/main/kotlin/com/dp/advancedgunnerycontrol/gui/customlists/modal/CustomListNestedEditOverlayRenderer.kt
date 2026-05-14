package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.modals.ShipViewModalFactory
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.min

internal object CustomListNestedEditOverlayRenderer {
    fun render(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        editParameterCount: Int,
        managerButtonStart: Int,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ) {
        buttons.subList(managerButtonStart.coerceIn(0, buttons.size), buttons.size)
            .forEach { button -> CampaignButtonSuppression.suppressCampaignButtonHover(button) }
        callbacks.addDialogShield(dialog, 0f, 0f, dialogWidth, dialogHeight)?.let { shield ->
            CampaignButtonControls.addControl(buttons, button = shield) {}
        }

        val editorWidth = min(CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH, dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING)
        val editorHeight = min(
            CustomListModalLayout.editModalHeight(editParameterCount),
            dialogHeight - 2f * CampaignGuiStyle.MODAL_PADDING,
        )
        val editor = ShipViewModalFactory.createDialog(
            parent = dialog,
            bounds = ShipViewModalFactory.centeredBounds(dialog, editorWidth, editorHeight),
            borderColor = CampaignGuiStyle.SAVE_BUTTON_HOVER_COLOR,
            fillColor = CampaignGuiStyle.MODAL_DIALOG_FILL_COLOR,
        )
        callbacks.renderEditor(editor, ship, context, editorWidth, editorHeight)
    }
}
