package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignMomentaryButton
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomTagManagerRowButton {
    fun add(
        parent: CustomPanelAPI,
        data: String,
        y: Float,
        rowWidth: Float,
        template: CampaignGuiStyle.ActionButtonTemplate,
        labelText: String,
        indentLevel: Int = 0,
        tooltip: String? = null,
        highlightTokens: List<String> = emptyList(),
        centerConfirmCancelText: Boolean = true,
        onClick: () -> Unit,
    ): CampaignMomentaryButton {
        val indent = indentLevel * CampaignGuiStyle.CHILD_ROW_INDENT
        return CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = parent,
            data = data,
            x = CustomListModalListRenderer.LIST_INSET + indent,
            y = y,
            width = rowWidth - CustomListModalListRenderer.LIST_INSET - indent,
            height = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            template = template,
            labelText = labelText,
            highlightTokens = highlightTokens,
            tooltip = tooltip,
            centerConfirmCancelText = centerConfirmCancelText,
            callback = onClick,
        )
    }
}
