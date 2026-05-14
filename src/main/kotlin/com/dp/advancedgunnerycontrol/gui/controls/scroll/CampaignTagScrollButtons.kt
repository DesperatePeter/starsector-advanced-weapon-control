package com.dp.advancedgunnerycontrol.gui.controls.scroll

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignMomentaryButton
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.rows.StyledCampaignButtonShell
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CampaignTagScrollButtons {
    fun addTagScrollIndicatorButton(
        parent: CustomPanelAPI,
        top: Float,
        symbol: String,
        data: Any,
        height: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
        x: Float = 0f,
        width: Float = parent.position.width,
    ): StyledCampaignButtonShell {
        val shell = CampaignActionRows.addStyledCampaignButtonShell(
            parent = parent,
            data = data,
            x = x,
            y = top,
            width = width,
            height = height,
            colors = CampaignGuiStyle.UNCOLOURED_BUTTON_COLORS,
            fillIdle = false
        )
        CampaignControlLabels.renderCenteredControlLabel(
            panel = shell.panel,
            text = symbol,
            width = width,
            height = height - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET,
        )
        return shell
    }

    fun addTagScrollIndicatorMomentaryButton(
        parent: CustomPanelAPI,
        top: Float,
        symbol: String,
        data: Any,
        height: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
        x: Float = 0f,
        width: Float = parent.position.width,
        callback: () -> Unit,
    ): Pair<StyledCampaignButtonShell, CampaignMomentaryButton> {
        val shell = addTagScrollIndicatorButton(
            parent = parent,
            top = top,
            symbol = symbol,
            data = data,
            height = height,
            x = x,
            width = width,
        )
        return shell to CampaignMomentaryButton(shell.button) { callback() }
    }
}
