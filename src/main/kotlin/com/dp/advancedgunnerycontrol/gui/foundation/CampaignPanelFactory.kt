package com.dp.advancedgunnerycontrol.gui.foundation

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType

import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CampaignPanelFactory {
    fun addPanel(
        parent: CustomPanelAPI,
        width: Float,
        height: Float,
        type: CampaignPanelType,
        x: Float,
        y: Float,
    ): CustomPanelAPI {
        val child = createChild(parent, width, height, type)
        child.position.inTL(x, y)
        return child
    }

    fun addPanelBelow(
        parent: CustomPanelAPI,
        anchor: CustomPanelAPI,
        width: Float,
        height: Float,
        type: CampaignPanelType,
        gap: Float = 0f,
    ): CustomPanelAPI {
        val child = createChild(parent, width, height, type)
        child.position.belowLeft(anchor, gap)
        return child
    }

    fun addPanelRightOf(
        parent: CustomPanelAPI,
        anchor: CustomPanelAPI,
        width: Float,
        height: Float,
        type: CampaignPanelType,
        gap: Float = 0f,
    ): CustomPanelAPI {
        val child = createChild(parent, width, height, type)
        child.position.rightOfTop(anchor, gap)
        return child
    }

    fun addBlackSpacerBelow(
        parent: CustomPanelAPI,
        anchor: CustomPanelAPI,
        width: Float,
        height: Float,
    ): CustomPanelAPI? {
        if (height <= 0.5f) return null
        val child = parent.createCustomPanel(
            width,
            height,
            CampaignPanelPlugin(
                CampaignPanelType.SHIP_MODES_PANEL,
                fillColor = CampaignGuiStyle.BLACK_PANEL_FILL_COLOR,
            )
        )
        parent.addComponent(child)
        child.position.belowLeft(anchor, 0f)
        return child
    }

    private fun createChild(
        parent: CustomPanelAPI,
        width: Float,
        height: Float,
        type: CampaignPanelType,
    ): CustomPanelAPI {
        val child = parent.createCustomPanel(width, height, CampaignPanelPlugin(type))
        parent.addComponent(child)
        return child
    }
}
