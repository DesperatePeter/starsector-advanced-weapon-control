package com.dp.advancedgunnerycontrol.gui.controls.shipmodes

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.scroll.PinnedVerticalScrollLayout
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalItemsHeight
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle

data class CampaignModeButtonGroupResult(
    val buttons: List<ButtonBase<*>>,
    val maxRowOffset: Int,
    val effectiveRowOffset: Int,
)

data class CampaignModeButtonGroupPlan(
    val sectionKey: String,
    val columns: Int,
    val itemWidth: Float,
    val activeModeSet: Set<String>,
    val layout: PinnedVerticalScrollLayout<CampaignModeListRow>,
)

sealed class CampaignModeListRow {
    data class Heading(
        val title: String,
        val expanded: Boolean,
        val active: Boolean,
    ) : CampaignModeListRow()

    data class Modes(
        val modes: List<String>,
    ) : CampaignModeListRow()
}

data class PreparedCampaignModeButtonGroup(
    val persistedModeList: List<String>,
    val plan: CampaignModeButtonGroupPlan,
) {
    val tightHeight: Float
        get() = computeVerticalItemsHeight(
            itemCount = plan.layout.scrollSlice.items.size +
                (if (plan.layout.scrollSlice.hasAbove) 1 else 0) +
                (if (plan.layout.scrollSlice.hasBelow) 1 else 0),
            itemHeight = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
            verticalGap = CampaignGuiStyle.SHIP_MODE_ITEM_VGAP,
        )
}
