package com.dp.advancedgunnerycontrol.gui.controls.panels

import com.dp.advancedgunnerycontrol.gui.foundation.addAgcHighlightedText
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CampaignModifiersPanel {
    const val HEIGHT = 48f

    fun buildCampaignModifiersPanel(panel: CustomPanelAPI, modifiersText: String) {
        val width = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val infoPanel = panel.createUIElement(width, HEIGHT, false)
        infoPanel.addAgcHighlightedText(
            modifiersText,
            0f,
            CampaignGuiStyle.MODIFIER_TEXT_COLOUR,
            "[SHIFT]",
            "[CTRL]"
        )
        panel.addUIElement(infoPanel).inTL(CampaignGuiStyle.PANEL_PADDING, 0f)
    }
}
