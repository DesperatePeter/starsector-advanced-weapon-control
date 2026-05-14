package com.dp.advancedgunnerycontrol.gui.shipview

import com.dp.advancedgunnerycontrol.gui.input.ShipViewInputController
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult
import com.dp.advancedgunnerycontrol.gui.options.panel.OptionsPanelRenderState
import com.dp.advancedgunnerycontrol.gui.options.panel.OptionsPanelRenderer
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

internal class ShipViewOptionsPanelController(
    initialScrollOffset: Int,
    private val panelPos: () -> PositionAPI?,
    private val removePanel: (CustomPanelAPI, CustomPanelAPI, String) -> Unit,
    private val onBeforeRefresh: () -> Unit,
) {
    private var scrollOffset = initialScrollOffset.coerceAtLeast(0)
    private var buildRows: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)? = null
    private var state: OptionsPanelRenderState? = null

    fun captureScrollOffset(): Int = scrollOffset

    fun startBuild(
        buildRows: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)?,
    ) {
        this.buildRows = buildRows
        state = null
    }

    fun clearForCollapsedPanel() {
        scrollOffset = 0
        state = null
    }

    fun build(
        panel: CustomPanelAPI,
        width: Float,
        height: Float,
        panelTop: Float,
        rootHeight: Float,
    ) {
        val rendered = OptionsPanelRenderer.build(
            panel = panel,
            width = width,
            height = height,
            panelTop = panelTop,
            rootHeight = rootHeight,
            rowOffset = scrollOffset,
            buildRows = buildRows,
            onScrollRows = ::scrollByRows,
        )
        state = rendered
        scrollOffset = rendered.effectiveScrollOffset
    }

    fun processScrollInput(events: MutableList<InputEventAPI>?): Boolean {
        return ShipViewInputController.processPanelScrollInput(
            events = events,
            panelPos = panelPos(),
            region = state?.scrollRegion,
            currentOffset = { scrollOffset },
            setOffset = { offset -> scrollOffset = offset },
            onScrolled = ::refresh,
        )
    }

    fun refresh() {
        onBeforeRefresh()
        val current = state ?: return
        val rendered = OptionsPanelRenderer.refresh(
            state = current,
            rowOffset = scrollOffset,
            buildRows = buildRows,
            removePanel = { parent, child -> removePanel(parent, child, "options rows") },
            onScrollRows = ::scrollByRows,
        )
        state = rendered
        scrollOffset = rendered.effectiveScrollOffset
    }

    private fun scrollByRows(delta: Int, maxOffset: Int) {
        ShipViewInputController.scrollByRows(
            currentOffset = scrollOffset,
            delta = delta,
            maxOffset = state?.maxScrollOffset ?: maxOffset,
            setOffset = { offset -> scrollOffset = offset },
            onScrolled = ::refresh,
        )
    }
}
