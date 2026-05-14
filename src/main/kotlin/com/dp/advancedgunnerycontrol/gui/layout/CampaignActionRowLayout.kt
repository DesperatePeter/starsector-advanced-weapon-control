package com.dp.advancedgunnerycontrol.gui.layout

import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffset
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*


import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import java.awt.Color
import kotlin.math.max

data class CampaignActionRowLayout<T>(
    val row: T,
    val wrappedText: String,
    val rowHeight: Float,
    val indent: Float = 0f,
)

data class CampaignActionPanelRows<T>(
    val layouts: List<CampaignActionRowLayout<T>>,
    val bottom: Float,
)

data class CampaignScrollableActionRows<T>(
    val offset: Int,
    val layouts: List<CampaignActionRowLayout<T>>,
    val maxOffset: Int,
)

data class CampaignScrollableActionRowsRenderResult(
    val offset: Int,
    val maxOffset: Int,
)

fun <T> computeCampaignActionRowLayouts(
    rows: List<T>,
    width: Float,
    maxLines: Int,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
    rowIndent: (T) -> Float = { 0f },
): List<CampaignActionRowLayout<T>> {
    return rows.map { row ->
        val indent = rowIndent(row).coerceAtLeast(0f)
        val rowWidth = max(1f, width - indent)
        val labelText = CampaignGuiStyle.actionLabelText(label(row), shortcuts(row))
        val layout = CampaignGuiStyle.actionLabelLayout(labelText, rowWidth, maxLines)
        CampaignActionRowLayout(row, layout.wrappedText, layout.rowHeight, indent)
    }
}

fun campaignActionRowsHeight(layouts: List<CampaignActionRowLayout<*>>): Float {
    val rowsHeight = layouts.sumOf { it.rowHeight.toDouble() }.toFloat()
    val rowGapsHeight = max(0, layouts.size - 1) * CampaignGuiStyle.ACTION_ROW_GAP
    return rowsHeight + rowGapsHeight
}

fun <T> computeCampaignScrollableActionRows(
    layouts: List<CampaignActionRowLayout<T>>,
    visibleHeight: Float,
    currentOffset: Int,
): CampaignScrollableActionRows<T> {
    if (layouts.isEmpty() || visibleHeight <= 0f) {
        return CampaignScrollableActionRows(0, emptyList(), 0)
    }

    var suffixHeight = 0f
    var suffixCount = 0
    for (layout in layouts.asReversed()) {
        val nextHeight = suffixHeight +
            if (suffixCount == 0) layout.rowHeight else CampaignGuiStyle.ACTION_ROW_GAP + layout.rowHeight
        if (suffixCount > 0 && nextHeight > visibleHeight) break
        suffixHeight = nextHeight
        suffixCount++
    }
    val maxOffset = max(0, layouts.size - max(1, suffixCount))
    val offset = usefulVerticalScrollOffset(currentOffset, maxOffset)

    val visible = mutableListOf<CampaignActionRowLayout<T>>()
    var usedHeight = 0f
    layouts.drop(offset).forEach { layout ->
        val nextHeight = usedHeight +
            if (visible.isEmpty()) layout.rowHeight else CampaignGuiStyle.ACTION_ROW_GAP + layout.rowHeight
        if (visible.isNotEmpty() && nextHeight > visibleHeight) return@forEach
        visible.add(layout)
        usedHeight = nextHeight
    }
    return CampaignScrollableActionRows(offset, visible, maxOffset)
}

fun <T> renderCampaignActionRows(
    panel: CustomPanelAPI,
    width: Float,
    top: Float,
    layouts: List<CampaignActionRowLayout<T>>,
    kind: (T) -> CampaignActionButtonKind,
    tooltip: (T) -> String,
    bindButton: (T, ButtonAPI) -> Unit,
    data: (T) -> Any = { it as Any },
    textColor: (T) -> Color = { CampaignGuiStyle.DEFAULT_TEXT_COLOUR },
    textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
): Float {
    var currentTop = top
    layouts.forEachIndexed { index, layout ->
        val row = layout.row
        val template = CampaignGuiStyle.actionButtonTemplate(kind(row)).copy(textColor = textColor(row))
        val shell = CampaignActionRows.addTemplatedCampaignActionRow(
            parent = panel,
            data = data(row),
            x = CampaignGuiStyle.PANEL_PADDING + layout.indent,
            y = currentTop,
            width = max(1f, width - layout.indent),
            height = layout.rowHeight,
            template = template,
            labelText = layout.wrappedText,
            highlightTokens = CampaignGuiStyle.ACTION_SHORTCUT_HIGHLIGHTS,
            tooltip = tooltip(row),
            textPadding = textPadding,
        )
        bindButton(row, shell.button)
        currentTop += layout.rowHeight
        if (index < layouts.lastIndex) {
            currentTop += CampaignGuiStyle.ACTION_ROW_GAP
        }
    }
    return currentTop
}

fun <T> renderCampaignScrollableActionRows(
    panel: CustomPanelAPI,
    width: Float,
    top: Float,
    layouts: List<CampaignActionRowLayout<T>>,
    visibleHeight: Float,
    currentOffset: Int,
    maxLines: Int,
    scrollIndicatorRow: (up: Boolean, visiblePageSize: Int, maxOffset: Int) -> T,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
    kind: (T) -> CampaignActionButtonKind,
    tooltip: (T) -> String,
    bindButton: (T, ButtonAPI) -> Unit,
    bindScrollIndicatorButton: (T, ButtonAPI) -> Unit,
    data: (T) -> Any = { it as Any },
    textColor: (T) -> Color = { CampaignGuiStyle.DEFAULT_TEXT_COLOUR },
    textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
): CampaignScrollableActionRowsRenderResult {
    var slice = computeCampaignScrollableActionRows(layouts, visibleHeight, currentOffset)
    var hasAbove = slice.offset > 0
    var hasBelow = slice.offset < slice.maxOffset
    repeat(2) {
        val indicatorHeight = campaignActionScrollIndicatorRowsHeight(
            width = width,
            maxLines = maxLines,
            hasAbove = hasAbove,
            hasBelow = hasBelow,
            scrollIndicatorRow = scrollIndicatorRow,
            label = label,
            shortcuts = shortcuts,
        )
        slice = computeCampaignScrollableActionRows(
            layouts = layouts,
            visibleHeight = max(CampaignGuiStyle.TAG_ITEM_HEIGHT, visibleHeight - indicatorHeight),
            currentOffset = currentOffset,
        )
        hasAbove = slice.offset > 0
        hasBelow = slice.offset < slice.maxOffset
    }

    val visiblePageSize = slice.layouts.size.coerceAtLeast(1)
    var currentTop = top
    if (hasAbove) {
        currentTop = renderCampaignActionScrollIndicatorRow(
            panel = panel,
            width = width,
            top = currentTop,
            maxLines = maxLines,
            up = true,
            visiblePageSize = visiblePageSize,
            maxOffset = slice.maxOffset,
            scrollIndicatorRow = scrollIndicatorRow,
            label = label,
            shortcuts = shortcuts,
            kind = kind,
            tooltip = tooltip,
            bindButton = bindScrollIndicatorButton,
            data = data,
            textColor = textColor,
            textPadding = textPadding,
        )
    }
    currentTop = renderCampaignActionRows(
        panel = panel,
        width = width,
        top = currentTop,
        layouts = slice.layouts,
        kind = kind,
        tooltip = tooltip,
        bindButton = bindButton,
        data = data,
        textColor = textColor,
        textPadding = textPadding,
    )
    if (hasBelow) {
        currentTop = renderCampaignActionScrollIndicatorRow(
            panel = panel,
            width = width,
            top = currentTop + CampaignGuiStyle.ACTION_ROW_GAP,
            maxLines = maxLines,
            up = false,
            visiblePageSize = visiblePageSize,
            maxOffset = slice.maxOffset,
            scrollIndicatorRow = scrollIndicatorRow,
            label = label,
            shortcuts = shortcuts,
            kind = kind,
            tooltip = tooltip,
            bindButton = bindScrollIndicatorButton,
            data = data,
            textColor = textColor,
            textPadding = textPadding,
        )
    }

    return CampaignScrollableActionRowsRenderResult(
        offset = slice.offset,
        maxOffset = slice.maxOffset,
    )
}

private fun <T> campaignActionScrollIndicatorRowsHeight(
    width: Float,
    maxLines: Int,
    hasAbove: Boolean,
    hasBelow: Boolean,
    scrollIndicatorRow: (up: Boolean, visiblePageSize: Int, maxOffset: Int) -> T,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
): Float {
    if (!hasAbove && !hasBelow) return 0f
    val indicatorHeight = campaignActionScrollIndicatorLayout(
        width = width,
        maxLines = maxLines,
        row = scrollIndicatorRow(true, 1, 0),
        label = label,
        shortcuts = shortcuts,
    ).rowHeight
    var height = 0f
    if (hasAbove) height += indicatorHeight + CampaignGuiStyle.ACTION_ROW_GAP
    if (hasBelow) height += indicatorHeight + CampaignGuiStyle.ACTION_ROW_GAP
    return height
}

private fun <T> renderCampaignActionScrollIndicatorRow(
    panel: CustomPanelAPI,
    width: Float,
    top: Float,
    maxLines: Int,
    up: Boolean,
    visiblePageSize: Int,
    maxOffset: Int,
    scrollIndicatorRow: (up: Boolean, visiblePageSize: Int, maxOffset: Int) -> T,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
    kind: (T) -> CampaignActionButtonKind,
    tooltip: (T) -> String,
    bindButton: (T, ButtonAPI) -> Unit,
    data: (T) -> Any,
    textColor: (T) -> Color,
    textPadding: Float,
): Float {
    val row = scrollIndicatorRow(up, visiblePageSize, maxOffset)
    val layout = campaignActionScrollIndicatorLayout(
        width = width,
        maxLines = maxLines,
        row = row,
        label = label,
        shortcuts = shortcuts,
    )
    return renderCampaignActionRows(
        panel = panel,
        width = width,
        top = top,
        layouts = listOf(layout),
        kind = kind,
        tooltip = tooltip,
        bindButton = bindButton,
        data = data,
        textColor = textColor,
        textPadding = textPadding,
    ) + CampaignGuiStyle.ACTION_ROW_GAP
}

private fun <T> campaignActionScrollIndicatorLayout(
    width: Float,
    maxLines: Int,
    row: T,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
): CampaignActionRowLayout<T> {
    return computeCampaignActionRowLayouts(
        rows = listOf(row),
        width = width,
        maxLines = maxLines,
        label = label,
        shortcuts = shortcuts,
    ).first().copy(row = row)
}

fun <T> renderCampaignActionPanelRows(
    panel: CustomPanelAPI,
    title: String,
    width: Float,
    headingHeight: Float,
    rows: List<T>,
    maxLines: Int,
    label: (T) -> String,
    shortcuts: (T) -> List<Int>,
    kind: (T) -> CampaignActionButtonKind,
    tooltip: (T) -> String,
    bindButton: (T, ButtonAPI) -> Unit,
    data: (T) -> Any = { it as Any },
    rowIndent: (T) -> Float = { 0f },
    textColor: (T) -> Color = { CampaignGuiStyle.DEFAULT_TEXT_COLOUR },
    textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
    addGapAfterRows: Boolean = true,
): CampaignActionPanelRows<T> {
    val layouts = computeCampaignActionRowLayouts(
        rows = rows,
        width = width,
        maxLines = maxLines,
        label = label,
        shortcuts = shortcuts,
        rowIndent = rowIndent,
    )
    return renderCampaignActionPanelLayouts(
        panel = panel,
        title = title,
        width = width,
        headingHeight = headingHeight,
        layouts = layouts,
        kind = kind,
        tooltip = tooltip,
        bindButton = bindButton,
        data = data,
        textColor = textColor,
        textPadding = textPadding,
        addGapAfterRows = addGapAfterRows,
    )
}

fun <T> renderCampaignActionPanelLayouts(
    panel: CustomPanelAPI,
    title: String,
    width: Float,
    headingHeight: Float,
    layouts: List<CampaignActionRowLayout<T>>,
    kind: (T) -> CampaignActionButtonKind,
    tooltip: (T) -> String,
    bindButton: (T, ButtonAPI) -> Unit,
    data: (T) -> Any = { it as Any },
    textColor: (T) -> Color = { CampaignGuiStyle.DEFAULT_TEXT_COLOUR },
    textPadding: Float = CampaignGuiStyle.ACTION_ROW_PADDING,
    addGapAfterRows: Boolean = true,
    renderHeading: Boolean = true,
): CampaignActionPanelRows<T> {
    if (renderHeading) {
        CampaignControlLabels.addCampaignPanelHeading(panel, title, headingHeight = headingHeight)
    }
    var bottom = renderCampaignActionRows(
        panel = panel,
        width = width,
        top = CampaignGuiStyle.PANEL_PADDING + headingHeight,
        layouts = layouts,
        kind = kind,
        tooltip = tooltip,
        bindButton = bindButton,
        data = data,
        textColor = textColor,
        textPadding = textPadding,
    )
    if (addGapAfterRows && layouts.isNotEmpty()) {
        bottom += CampaignGuiStyle.ACTION_ROW_GAP
    }
    return CampaignActionPanelRows(layouts, bottom)
}
