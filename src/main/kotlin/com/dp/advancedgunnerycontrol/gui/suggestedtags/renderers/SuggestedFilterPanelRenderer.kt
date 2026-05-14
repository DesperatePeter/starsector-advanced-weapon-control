package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.options.SuggestedFilterPanelRendererCore
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

data class SuggestedFilterPanelResult(
    val bottom: Float,
    val scrollOffset: Int,
    val scrollRegion: VerticalScrollRegion<Unit>,
)

/**
 * Suggested-tags Filter panel component.
 * Renders active filters, filter category headings, and scroll indicators in
 * the left column of Customize Suggested Tags.
 */
fun renderSuggestedFilterPanel(
    panel: CustomPanelAPI,
    width: Float,
    top: Float,
    maxVisibleHeight: Float,
    filterRows: List<SuggestedTagUiAction>,
    scrollOffset: Int,
    bindButton: (SuggestedTagUiAction, ButtonAPI, Boolean) -> Unit,
    onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
): SuggestedFilterPanelResult =
    SuggestedFilterPanelRendererCore.renderSuggestedFilterPanel(
        panel = panel,
        width = width,
        top = top,
        maxVisibleHeight = maxVisibleHeight,
        filterRows = filterRows,
        scrollOffset = scrollOffset,
        bindButton = bindButton,
        onScrollIndicator = onScrollIndicator,
    )
