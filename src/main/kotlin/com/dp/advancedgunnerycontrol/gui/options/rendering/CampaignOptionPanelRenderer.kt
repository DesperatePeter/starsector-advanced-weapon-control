package com.dp.advancedgunnerycontrol.gui.options.rendering

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.layout.renderCampaignScrollableActionRows
import com.dp.advancedgunnerycontrol.gui.options.controllers.bindCampaignOptionRowButton
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsLayout
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult

import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

object CampaignOptionPanelRenderer {
    fun renderCampaignOptionsPanel(
        panel: CustomPanelAPI,
        rows: List<CampaignOptionRow>,
        renderHeading: Boolean,
        rowOffset: Int,
        visibleBodyHeight: Float?,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
        bindButton: (CampaignOptionRow, ButtonAPI) -> Unit,
    ): CampaignOptionsRenderResult {
        val width = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val layout = CampaignOptionPanelLayout.computeCampaignOptionsLayout(width, rows)
        return renderCampaignOptionsPanelLayout(
            panel = panel,
            layout = layout,
            renderHeading = renderHeading,
            rowOffset = rowOffset,
            visibleBodyHeight = visibleBodyHeight,
            onScrollIndicator = onScrollIndicator,
            bindButton = bindButton,
        )
    }

    fun renderCampaignOptionsPanelLayout(
        panel: CustomPanelAPI,
        layout: CampaignOptionsLayout,
        renderHeading: Boolean,
        rowOffset: Int,
        visibleBodyHeight: Float?,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
        bindButton: (CampaignOptionRow, ButtonAPI) -> Unit,
    ): CampaignOptionsRenderResult {
        val width = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val bodyHeight = visibleBodyHeight ?: panel.position.height
        if (renderHeading) {
            CampaignControlLabels.addCampaignPanelHeading(
                panel = panel,
                title = "Options",
                headingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT,
            )
        }
        val rendered = renderCampaignScrollableActionRows(
            panel = panel,
            width = width,
            top = CampaignGuiStyle.PANEL_PADDING + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT,
            layouts = layout.rows,
            visibleHeight = bodyHeight,
            currentOffset = rowOffset,
            maxLines = CampaignGuiStyle.ACTION_LABEL_MAX_LINES,
            scrollIndicatorRow = { up, visiblePageSize, maxOffset ->
                campaignOptionScrollIndicatorRow(up, visiblePageSize, maxOffset, onScrollIndicator)
            },
            label = { row -> row.label },
            shortcuts = { row -> row.shortcut?.let(::listOf) ?: emptyList() },
            kind = { row -> row.kind },
            tooltip = { row -> row.tooltip },
            bindButton = bindButton,
            bindScrollIndicatorButton = bindButton,
            textColor = { row -> row.textColor },
            textPadding = CampaignGuiStyle.ACTION_ROW_PADDING,
        )
        return CampaignOptionsRenderResult(
            rowOffset = rendered.offset,
            maxRowOffset = rendered.maxOffset
        )
    }

    fun renderBoundCampaignOptionsPanel(
        panel: CustomPanelAPI,
        layout: CampaignOptionsLayout,
        optionButtons: MutableList<ButtonBase<*>>,
        executeRow: (CampaignOptionRow) -> Unit,
        renderHeading: Boolean = true,
        rowOffset: Int = 0,
        visibleBodyHeight: Float? = null,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
        bindListener: ((ButtonAPI) -> Unit)? = null,
        afterRightClick: (CampaignOptionRow) -> Unit = {},
    ): CampaignOptionsRenderResult {
        optionButtons.clear()
        return renderCampaignOptionsPanelLayout(
            panel = panel,
            layout = layout,
            renderHeading = renderHeading,
            rowOffset = rowOffset,
            visibleBodyHeight = visibleBodyHeight,
            onScrollIndicator = onScrollIndicator,
            bindButton = { row, button ->
                optionButtons.bindCampaignOptionRowButton(
                    row = row,
                    button = button,
                    executeRow = executeRow,
                    afterRightClick = afterRightClick,
                )
                bindListener?.invoke(button)
            },
        )
    }

    private fun campaignOptionScrollIndicatorRow(
        up: Boolean,
        visiblePageSize: Int,
        maxOffset: Int,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): CampaignOptionRow {
        val label = if (up) CampaignGuiStyle.SCROLL_INDICATOR_ABOVE else CampaignGuiStyle.SCROLL_INDICATOR_BELOW
        val tooltip = if (up) "Show earlier option rows." else "Show later option rows."
        return CampaignOptionRow(
            label = label,
            tooltip = tooltip,
            kind = CampaignActionButtonKind.UNCOLOURED,
            rebuildAfter = false,
        ) {
            val delta = if (up) -visiblePageSize else visiblePageSize
            onScrollIndicator(delta, maxOffset)
        }
    }
}
