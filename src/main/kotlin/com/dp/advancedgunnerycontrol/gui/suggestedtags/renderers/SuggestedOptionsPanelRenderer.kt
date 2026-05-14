package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.options.SuggestedOptionsPanelRendererCore
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

data class SuggestedOptionsPanelResult(
    val actionScrollOffset: Int,
    val actionScrollRegion: VerticalScrollRegion<Unit>?,
    val filterScrollOffset: Int,
    val filterScrollRegion: VerticalScrollRegion<Unit>,
)

/**
 * Suggested-tags left-column component.
 * Renders Backup/Reset/Restore, page controls, tag-list selector, Reset Filters,
 * the Filter panel, and page/status text.
 */
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
): SuggestedOptionsPanelResult =
    SuggestedOptionsPanelRendererCore.renderSuggestedOptionsPanel(
        panel = panel,
        actions = actions,
        filterRows = filterRows,
        pageString = pageString,
        actionScrollOffset = actionScrollOffset,
        filterScrollOffset = filterScrollOffset,
        bindButton = bindButton,
        onActionScrollIndicator = onActionScrollIndicator,
        onFilterScrollIndicator = onFilterScrollIndicator,
    )
