package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.options

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction

internal object SuggestedOptionScrollIndicators {
    fun suggestedActionScrollIndicatorAction(
        up: Boolean,
        visiblePageSize: Int,
        maxOffset: Int,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): SuggestedTagUiAction {
        val label = if (up) CampaignGuiStyle.SCROLL_INDICATOR_ABOVE else CampaignGuiStyle.SCROLL_INDICATOR_BELOW
        val tooltip = if (up) "Show earlier option rows." else "Show later option rows."
        return SuggestedTagUiAction(label, tooltip = tooltip) {
            val delta = if (up) -visiblePageSize else visiblePageSize
            onScrollIndicator(delta, maxOffset)
        }
    }

    fun suggestedFilterScrollIndicatorAction(
        up: Boolean,
        visiblePageSize: Int,
        maxOffset: Int,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): SuggestedTagUiAction {
        val label = if (up) CampaignGuiStyle.SCROLL_INDICATOR_ABOVE else CampaignGuiStyle.SCROLL_INDICATOR_BELOW
        val tooltip = if (up) "Show earlier filter rows." else "Show later filter rows."
        return SuggestedTagUiAction(label, tooltip = tooltip) {
            val delta = if (up) -visiblePageSize else visiblePageSize
            onScrollIndicator(delta, maxOffset)
        }
    }
}
