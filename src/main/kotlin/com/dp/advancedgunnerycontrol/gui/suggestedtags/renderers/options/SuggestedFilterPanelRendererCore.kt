package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.options

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels
import com.dp.advancedgunnerycontrol.gui.layout.computeCampaignActionRowLayouts
import com.dp.advancedgunnerycontrol.gui.layout.renderCampaignScrollableActionRows
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.SuggestedFilterPanelResult
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max

internal object SuggestedFilterPanelRendererCore {
    fun renderSuggestedFilterPanel(
        panel: CustomPanelAPI,
        width: Float,
        top: Float,
        maxVisibleHeight: Float,
        filterRows: List<SuggestedTagUiAction>,
        scrollOffset: Int,
        bindButton: (SuggestedTagUiAction, ButtonAPI, Boolean) -> Unit,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): SuggestedFilterPanelResult {
        CampaignControlLabels.addCampaignPanelHeading(panel, "Filter", top = top, headingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT)

        val layouts = computeCampaignActionRowLayouts(
            rows = filterRows,
            width = width,
            maxLines = CampaignGuiStyle.SUGGESTED_ACTION_LABEL_MAX_LINES,
            label = { action -> action.name },
            shortcuts = { action -> action.shortcuts },
            rowIndent = { action -> action.indent },
        )
        val rowsTop = top + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        val visibleHeight = max(
            CampaignGuiStyle.TAG_ITEM_HEIGHT,
            panel.position.height - rowsTop - CampaignGuiStyle.SUGGESTED_FILTER_INFO_HEIGHT - CampaignGuiStyle.PANEL_PADDING
        ).coerceAtMost(maxVisibleHeight.coerceAtLeast(0f))
        val relativeBottom = panel.position.height - rowsTop - visibleHeight
        val renderedRows = renderCampaignScrollableActionRows(
            panel = panel,
            width = width,
            top = rowsTop,
            layouts = layouts,
            visibleHeight = visibleHeight,
            currentOffset = scrollOffset,
            maxLines = CampaignGuiStyle.SUGGESTED_ACTION_LABEL_MAX_LINES,
            scrollIndicatorRow = { up, visiblePageSize, maxOffset ->
                SuggestedOptionScrollIndicators.suggestedFilterScrollIndicatorAction(
                    up,
                    visiblePageSize,
                    maxOffset,
                    onScrollIndicator,
                )
            },
            label = { action -> action.name },
            shortcuts = { action -> action.shortcuts },
            kind = { action -> action.effectiveKind() },
            tooltip = { action -> action.tooltip },
            bindButton = { action, button ->
                bindButton(action, button, true)
            },
            bindScrollIndicatorButton = { action, button ->
                bindButton(action, button, false)
            },
            textPadding = CampaignGuiStyle.ACTION_ROW_PADDING,
        )
        val scrollRegion = VerticalScrollRegion(
            key = Unit,
            left = CampaignGuiStyle.PANEL_PADDING,
            right = CampaignGuiStyle.PANEL_PADDING + width,
            bottom = relativeBottom,
            top = relativeBottom + visibleHeight,
            maxOffset = renderedRows.maxOffset,
        )

        return SuggestedFilterPanelResult(
            bottom = rowsTop + visibleHeight + CampaignGuiStyle.ACTION_ROW_GAP,
            scrollOffset = renderedRows.offset,
            scrollRegion = scrollRegion,
        )
    }
}
