package com.dp.advancedgunnerycontrol.gui.customlists.review

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListChangeReviewFooterRenderer {
    fun render(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ) {
        CustomListModalFooterRenderer.addEdgeButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            left = ModalFooterButtonSpec(
                data = "custom_tag_change_confirm",
                kind = CampaignActionButtonKind.CONFIRM,
                labelText = "Confirm",
                tooltip = "Apply pending Custom tag and ship-mode additions, modifications, and removals.",
            ) {
                callbacks.applyChanges(ship, context)
            },
            right = ModalFooterButtonSpec(
                data = "custom_tag_change_go_back",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Go Back",
                tooltip = "Return to the Custom list editor without applying changes.",
            ) {
                callbacks.switchMode(CustomListModalMode.MANAGE_TAGS)
                callbacks.refresh()
            },
        )
    }
}
