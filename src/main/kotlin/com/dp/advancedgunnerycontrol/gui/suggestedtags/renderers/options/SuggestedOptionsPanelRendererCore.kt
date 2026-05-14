package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.options

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels
import com.dp.advancedgunnerycontrol.gui.foundation.addAgcText
import com.dp.advancedgunnerycontrol.gui.layout.CampaignScrollableActionRowsRenderResult
import com.dp.advancedgunnerycontrol.gui.layout.campaignActionRowsHeight
import com.dp.advancedgunnerycontrol.gui.layout.computeCampaignActionRowLayouts
import com.dp.advancedgunnerycontrol.gui.layout.renderCampaignScrollableActionRows
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.SuggestedOptionsPanelResult
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max
import kotlin.math.min

internal object SuggestedOptionsPanelRendererCore {
    fun renderSuggestedOptionsPanel(
        panel: CustomPanelAPI,
        actions: List<SuggestedTagUiAction>,
        filterRows: List<SuggestedTagUiAction>,
        pageString: String,
        actionScrollOffset: Int,
        filterScrollOffset: Int,
        bindButton: (SuggestedTagUiAction, ButtonAPI, Boolean) -> Unit,
        onActionScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
        onFilterScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): SuggestedOptionsPanelResult {
        val width = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val headingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        CampaignControlLabels.addCampaignPanelHeading(panel, "Options", headingHeight = headingHeight)

        val actionLayouts = computeCampaignActionRowLayouts(
            rows = actions,
            width = width,
            maxLines = CampaignGuiStyle.SUGGESTED_ACTION_LABEL_MAX_LINES,
            label = { action -> action.name },
            shortcuts = { action -> action.shortcuts },
        )
        val actionRowsTop = CampaignGuiStyle.PANEL_PADDING + headingHeight
        val pageStatusHeight = CampaignGuiStyle.SUGGESTED_FILTER_INFO_HEIGHT
        val filterMinimumHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT + CampaignGuiStyle.TAG_ITEM_HEIGHT
        val maxActionRowsHeight = (panel.position.height -
            actionRowsTop -
            filterMinimumHeight -
            pageStatusHeight -
            3f * CampaignGuiStyle.PANEL_PADDING
        ).coerceAtLeast(0f)
        val desiredActionRowsHeight = min(
            campaignActionRowsHeight(actionLayouts),
            panel.position.height * 0.35f
        )
        val actionVisibleHeight = min(desiredActionRowsHeight, maxActionRowsHeight).coerceAtLeast(0f)
        val actionRenderResult = if (actionVisibleHeight > 0f) {
            renderCampaignScrollableActionRows(
                panel = panel,
                width = width,
                top = actionRowsTop,
                layouts = actionLayouts,
                visibleHeight = actionVisibleHeight,
                currentOffset = actionScrollOffset,
                maxLines = CampaignGuiStyle.SUGGESTED_ACTION_LABEL_MAX_LINES,
                scrollIndicatorRow = { up, visiblePageSize, maxOffset ->
                    SuggestedOptionScrollIndicators.suggestedActionScrollIndicatorAction(
                        up,
                        visiblePageSize,
                        maxOffset,
                        onActionScrollIndicator,
                    )
                },
                label = { action -> action.name },
                shortcuts = { action -> action.shortcuts },
                kind = { action -> action.effectiveKind() },
                tooltip = { action -> action.tooltip },
                bindButton = { action, button ->
                    bindButton(action, button, false)
                },
                bindScrollIndicatorButton = { action, button ->
                    bindButton(action, button, false)
                },
            )
        } else {
            CampaignScrollableActionRowsRenderResult(0, 0)
        }
        val actionScrollRegion = actionScrollRegion(panel, width, actionRowsTop, actionVisibleHeight, actionRenderResult.maxOffset)

        val filterTop = actionRowsTop +
            actionVisibleHeight +
            if (actionLayouts.isEmpty()) 0f else CampaignGuiStyle.PANEL_PADDING
        val filterMaxVisibleHeight = (panel.position.height -
            filterTop -
            headingHeight -
            pageStatusHeight -
            CampaignGuiStyle.PANEL_PADDING
        ).coerceAtLeast(0f)
        val filterResult = SuggestedFilterPanelRendererCore.renderSuggestedFilterPanel(
            panel = panel,
            width = width,
            top = filterTop,
            maxVisibleHeight = filterMaxVisibleHeight,
            filterRows = filterRows,
            scrollOffset = filterScrollOffset,
            bindButton = bindButton,
            onScrollIndicator = onFilterScrollIndicator,
        )
        val infoTop = filterResult.bottom
        val infoHeight = max(0f, panel.position.height - infoTop - CampaignGuiStyle.PANEL_PADDING)
        if (infoHeight > 0f) {
            val infoPanel = panel.createUIElement(width, infoHeight, false)
            infoPanel.addAgcText(pageString, 4f)
            panel.addUIElement(infoPanel).inTL(CampaignGuiStyle.PANEL_PADDING, infoTop)
        }

        return SuggestedOptionsPanelResult(
            actionScrollOffset = actionRenderResult.offset,
            actionScrollRegion = actionScrollRegion,
            filterScrollOffset = filterResult.scrollOffset,
            filterScrollRegion = filterResult.scrollRegion,
        )
    }

    private fun actionScrollRegion(
        panel: CustomPanelAPI,
        width: Float,
        actionRowsTop: Float,
        actionVisibleHeight: Float,
        maxOffset: Int,
    ): VerticalScrollRegion<Unit>? {
        if (maxOffset <= 0 || actionVisibleHeight <= 0f) return null
        val actionBottom = panel.position.height - actionRowsTop - actionVisibleHeight
        return VerticalScrollRegion(
            key = Unit,
            left = CampaignGuiStyle.PANEL_PADDING,
            right = CampaignGuiStyle.PANEL_PADDING + width,
            bottom = actionBottom,
            top = actionBottom + actionVisibleHeight,
            maxOffset = maxOffset,
        )
    }
}
