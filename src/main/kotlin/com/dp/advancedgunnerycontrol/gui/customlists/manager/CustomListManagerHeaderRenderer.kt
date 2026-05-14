package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.foundation.addAgcText
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListManagerHeaderRenderer {
    fun render(dialog: CustomPanelAPI, dialogWidth: Float): Float {
        val headerLayout = CustomListModalLayout.managerHeaderLayout(dialogWidth)
        CustomListModalTitleRenderer.renderTitle(dialog, dialogWidth, "Manage Tags and Ship Modes")

        val instructions = dialog.createUIElement(
            dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            headerLayout.instructionLayout.renderHeight,
            false,
        )
        instructions.setParaFontDefault()
        instructions.addAgcText(headerLayout.instructionLayout.wrappedText, 0f)
        dialog.addUIElement(instructions).inTL(
            CampaignGuiStyle.MODAL_PADDING,
            CampaignGuiStyle.MODAL_PADDING + headerLayout.instructionTop,
        )
        return headerLayout.renderHeight
    }
}
