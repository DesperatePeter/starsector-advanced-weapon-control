package com.dp.advancedgunnerycontrol.gui.options.panel

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult

import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max

internal data class OptionsPanelRenderState(
    val panel: CustomPanelAPI,
    val rowsPanel: CustomPanelAPI?,
    val width: Float,
    val height: Float,
    val panelTop: Float,
    val rootHeight: Float,
    val bodyTop: Float,
    val bodyHeight: Float,
    val scrollRegion: VerticalScrollRegion<Unit>?,
    val maxScrollOffset: Int,
    val effectiveScrollOffset: Int,
)

internal object OptionsPanelRenderer {
    fun build(
        panel: CustomPanelAPI,
        width: Float,
        height: Float,
        panelTop: Float,
        rootHeight: Float,
        rowOffset: Int,
        buildRows: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)?,
        onScrollRows: (Int, Int) -> Unit,
    ): OptionsPanelRenderState {
        val bodyTop = CampaignGuiStyle.PANEL_PADDING + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        val bodyHeight = max(0f, height - bodyTop - CampaignGuiStyle.PANEL_PADDING)
        return renderRows(
            panel = panel,
            width = width,
            height = height,
            panelTop = panelTop,
            rootHeight = rootHeight,
            bodyTop = bodyTop,
            bodyHeight = bodyHeight,
            rowOffset = rowOffset,
            buildRows = buildRows,
            onScrollRows = onScrollRows,
        )
    }

    fun refresh(
        state: OptionsPanelRenderState,
        rowOffset: Int,
        buildRows: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)?,
        removePanel: (CustomPanelAPI, CustomPanelAPI) -> Unit,
        onScrollRows: (Int, Int) -> Unit,
    ): OptionsPanelRenderState {
        state.rowsPanel?.let { rowsPanel -> removePanel(state.panel, rowsPanel) }
        return renderRows(
            panel = state.panel,
            width = state.width,
            height = state.height,
            panelTop = state.panelTop,
            rootHeight = state.rootHeight,
            bodyTop = state.bodyTop,
            bodyHeight = state.bodyHeight,
            rowOffset = rowOffset,
            buildRows = buildRows,
            onScrollRows = onScrollRows,
        )
    }

    private fun renderRows(
        panel: CustomPanelAPI,
        width: Float,
        height: Float,
        panelTop: Float,
        rootHeight: Float,
        bodyTop: Float,
        bodyHeight: Float,
        rowOffset: Int,
        buildRows: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)?,
        onScrollRows: (Int, Int) -> Unit,
    ): OptionsPanelRenderState {
        buildRows ?: return OptionsPanelRenderState(
            panel = panel,
            rowsPanel = null,
            width = width,
            height = height,
            panelTop = panelTop,
            rootHeight = rootHeight,
            bodyTop = bodyTop,
            bodyHeight = bodyHeight,
            scrollRegion = null,
            maxScrollOffset = 0,
            effectiveScrollOffset = 0,
        )

        val rowsPanel = panel.createCustomPanel(width, height, null)
        panel.addComponent(rowsPanel)
        rowsPanel.position.inTL(0f, 0f)
        val result = buildRows(rowsPanel, false, rowOffset, bodyHeight, onScrollRows)
        val maxScrollOffset = result.maxRowOffset
        val effectiveScrollOffset = result.rowOffset.coerceIn(0, maxScrollOffset)
        return OptionsPanelRenderState(
            panel = panel,
            rowsPanel = rowsPanel,
            width = width,
            height = height,
            panelTop = panelTop,
            rootHeight = rootHeight,
            bodyTop = bodyTop,
            bodyHeight = bodyHeight,
            scrollRegion = VerticalScrollRegion(
                key = Unit,
                left = CampaignGuiStyle.PANEL_PADDING,
                right = width - CampaignGuiStyle.PANEL_PADDING,
                bottom = rootHeight - panelTop - bodyTop - bodyHeight,
                top = rootHeight - panelTop - bodyTop,
                maxOffset = maxScrollOffset,
            ),
            maxScrollOffset = maxScrollOffset,
            effectiveScrollOffset = effectiveScrollOffset,
        )
    }
}
