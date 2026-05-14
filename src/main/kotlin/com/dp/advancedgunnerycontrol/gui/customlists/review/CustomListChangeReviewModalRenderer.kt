package com.dp.advancedgunnerycontrol.gui.customlists.review

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListChangeReviewModalRenderer {
    fun renderChangeConfirmation(
        dialog: CustomPanelAPI,
        ship: FleetMemberAPI,
        context: ShipEditorPersistenceContext,
        dialogWidth: Float,
        dialogHeight: Float,
        rows: List<CustomTagChangeReviewRow>,
        hasRemovals: Boolean,
        state: CustomListChangeReviewState,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListModalScrollRegion? {
        CustomListModalTitleRenderer.renderTitle(
            dialog = dialog,
            dialogWidth = dialogWidth,
            title = if (hasRemovals) "Custom List Changes Warning" else "Custom List Changes",
            color = if (hasRemovals) CampaignGuiStyle.ALERT_RED_COLOR else CampaignGuiStyle.CONFIRM_BUTTON_HOVER_COLOR,
        )

        val scrollRegion = CustomListChangeReviewListRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            rows = rows,
            state = state,
            buttons = buttons,
            callbacks = callbacks,
        )
        CustomListChangeReviewFooterRenderer.render(
            dialog = dialog,
            ship = ship,
            context = context,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            callbacks = callbacks,
        )
        return scrollRegion
    }
}
