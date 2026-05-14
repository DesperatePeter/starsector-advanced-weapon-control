package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.foundation.addAgcLargeHeading
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI
import java.awt.Color

internal object CustomListModalTitleRenderer {
    fun renderTitle(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        title: String,
        color: Color = CampaignGuiStyle.SAVE_BUTTON_HOVER_COLOR,
    ) {
        val text = dialog.createUIElement(
            dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            CampaignGuiStyle.MODAL_HEADING_HEIGHT,
            false,
        )
        text.addAgcLargeHeading(title, color)
        dialog.addUIElement(text).inTL(CampaignGuiStyle.MODAL_PADDING, CampaignGuiStyle.MODAL_PADDING)
    }
}
