package com.dp.advancedgunnerycontrol.gui.foundation

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle

import java.awt.Color
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun TooltipMakerAPI.applyAgcDefaultTextStyle() {
    CampaignGuiStyle.DEFAULT_TEXT_FONT?.let { setParaFont(it) }
}

fun TooltipMakerAPI.applyAgcTooltipTextStyle() {
    // Tooltip panels otherwise keep Starsector's smaller tooltip paragraph
    // default. setParaFontDefault() keeps the default family but bumps it to
    // the normal paragraph size; avoid explicit Victor font swaps here.
    CampaignGuiStyle.TOOLTIP_TEXT_FONT?.let { setParaFont(it) } ?: setParaFontDefault()
    setParaFontColor(CampaignGuiStyle.TOOLTIP_TEXT_COLOR)
}

fun TooltipMakerAPI.addAgcText(
    text: String,
    pad: Float = 0f,
    textColor: Color? = null,
): LabelAPI {
    applyAgcDefaultTextStyle()
    return if (textColor == null) {
        addPara(text, pad)
    } else {
        addPara(text, textColor, pad)
    }
}

fun TextPanelAPI.addAgcText(text: String) {
    addPara(text)
}

fun TooltipMakerAPI.addAgcHighlightedText(
    text: String,
    pad: Float,
    highlightColor: Color,
    vararg highlights: String,
): LabelAPI {
    applyAgcDefaultTextStyle()
    return addPara(text, pad, highlightColor, *highlights)
}

fun TooltipMakerAPI.addAgcLargeHeading(
    text: String,
    textColor: Color,
    pad: Float = 0f,
): LabelAPI {
    setParaInsigniaLarge()
    return addPara(text, textColor, pad)
}

data class WrapGridMetrics(
    val columns: Int,
    val rows: Int,
    val itemWidth: Float,
    val itemHeight: Float,
    val horizontalGap: Float,
    val verticalGap: Float,
) {
    fun xFor(index: Int): Float = (index % columns) * (itemWidth + horizontalGap)
    fun yFor(index: Int): Float = (index / columns) * (itemHeight + verticalGap)
}

data class WrappedLabelLayout(
    val wrappedText: String,
    val lineCount: Int,
    val rowHeight: Float,
    val textWidth: Float = 0f,
    val renderHeight: Float = rowHeight,
    val wasTruncated: Boolean = false,
)

private data class FittedTextLines(
    val lines: List<String>,
    val wasTruncated: Boolean,
)

fun computeWrapGridMetrics(
    itemCount: Int,
    availableWidth: Float,
    availableHeight: Float,
    minItemWidth: Float,
    itemHeight: Float,
    horizontalGap: Float,
    verticalGap: Float,
    maxColumns: Int = itemCount,
): WrapGridMetrics {
    if (itemCount <= 0) {
        return WrapGridMetrics(1, 0, availableWidth, itemHeight, horizontalGap, verticalGap)
    }

    val widthLimitedColumns = max(
        1,
        floor((availableWidth + horizontalGap) / (minItemWidth + horizontalGap)).toInt()
    )
    val upperColumns = min(maxColumns, max(1, widthLimitedColumns))
    var chosen = WrapGridMetrics(
        columns = 1,
        rows = itemCount,
        itemWidth = availableWidth,
        itemHeight = itemHeight,
        horizontalGap = horizontalGap,
        verticalGap = verticalGap,
    )

    for (columns in upperColumns downTo 1) {
        val itemWidth = (availableWidth - horizontalGap * (columns - 1)) / columns
        if (itemWidth <= 0f) continue
        val rows = ceil(itemCount.toFloat() / columns.toFloat()).toInt()
        val requiredHeight = rows * itemHeight + max(0, rows - 1) * verticalGap
        chosen = WrapGridMetrics(columns, rows, itemWidth, itemHeight, horizontalGap, verticalGap)
        if (requiredHeight <= availableHeight) {
            return chosen
        }
    }

    return chosen
}

private fun wrapLongToken(token: String, maxCharsPerLine: Int): List<String> {
    if (token.length <= maxCharsPerLine) return listOf(token)
    if (maxCharsPerLine <= 1) return token.map { it.toString() }
    val chunks = mutableListOf<String>()
    var index = 0
    while (index < token.length) {
        val end = min(token.length, index + maxCharsPerLine)
        chunks.add(token.substring(index, end))
        index = end
    }
    return chunks
}

private fun wrapTextLineWordAware(line: String, maxCharsPerLine: Int): List<String> {
    if (line.isBlank()) return listOf("")
    if (!line.contains(" ")) return wrapLongToken(line, maxCharsPerLine)

    val wrapped = mutableListOf<String>()
    var current = ""
    line.split(Regex("\\s+")).filter { it.isNotBlank() }.forEach { word ->
        val segments = if (word.length > maxCharsPerLine) wrapLongToken(word, maxCharsPerLine) else listOf(word)
        segments.forEachIndexed { segmentIndex, segment ->
            if (current.isBlank()) {
                current = segment
            } else if ((current.length + 1 + segment.length) <= maxCharsPerLine) {
                current += " $segment"
            } else {
                wrapped.add(current)
                current = segment
            }
            if (segmentIndex < segments.lastIndex) {
                wrapped.add(current)
                current = ""
            }
        }
    }
    if (current.isNotBlank()) wrapped.add(current)
    return if (wrapped.isEmpty()) listOf("") else rebalanceWeakLineEndings(wrapped, maxCharsPerLine)
}

private fun rebalanceWeakLineEndings(lines: List<String>, maxCharsPerLine: Int): List<String> {
    if (lines.size <= 1) return lines
    val rebalanced = lines.toMutableList()
    var index = 0
    while (index < rebalanced.lastIndex) {
        val words = rebalanced[index].split(Regex("\\s+")).filter { it.isNotBlank() }
        val carryCount = weakTrailingWordCount(words)
        if (carryCount > 0 && carryCount < words.size) {
            val kept = words.dropLast(carryCount).joinToString(" ")
            val carried = words.takeLast(carryCount).joinToString(" ")
            rebalanced[index] = kept
            rebalanced[index + 1] = "$carried ${rebalanced[index + 1]}".trim()
        }
        carryOverflowingWordsForward(rebalanced, index + 1, maxCharsPerLine)
        index++
    }
    return rebalanced
}

private fun carryOverflowingWordsForward(lines: MutableList<String>, startIndex: Int, maxCharsPerLine: Int) {
    var index = startIndex
    while (index < lines.size) {
        val words = lines[index].split(Regex("\\s+")).filter { it.isNotBlank() }
        if (lines[index].length <= maxCharsPerLine || words.size <= 1) {
            index++
            continue
        }
        val carried = words.last()
        lines[index] = words.dropLast(1).joinToString(" ")
        if (index == lines.lastIndex) {
            lines.add(carried)
        } else {
            lines[index + 1] = "$carried ${lines[index + 1]}".trim()
        }
        if (lines[index].length > maxCharsPerLine) continue
        index++
    }
}

private fun weakTrailingWordCount(words: List<String>): Int {
    if (words.size >= 2 && words[words.lastIndex - 1].equals("as", ignoreCase = true)) {
        val last = words.last().trimEnd(',', '.', ';', ':')
        if (last.equals("the", ignoreCase = true) ||
            last.equals("a", ignoreCase = true) ||
            last.equals("an", ignoreCase = true)
        ) {
            return 2
        }
    }
    val last = words.lastOrNull()?.trimEnd(',', '.', ';', ':') ?: return 0
    return if (last.lowercase() in weakLineEndingWords) 1 else 0
}

private fun truncateLineToFit(
    text: String,
    maxVisibleLength: Int,
    longTokenBreakLength: Int = 10,
): String {
    val normalized = text.trim()
    if (maxVisibleLength <= 0) return ""
    if (normalized.length <= maxVisibleLength) return normalized
    if (maxVisibleLength <= 3) return normalized.take(maxVisibleLength)

    val words = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.size <= 1) {
        return normalized.take(max(1, maxVisibleLength - 3)) + "..."
    }

    val kept = mutableListOf<String>()
    for (word in words) {
        val candidate = (kept + word).joinToString(" ")
        if (candidate.length + 4 <= maxVisibleLength) {
            kept.add(word)
            continue
        }
        if (kept.isEmpty() || word.length >= longTokenBreakLength) {
            val partialCandidate = if (kept.isEmpty()) word else (kept + word).joinToString(" ")
            return partialCandidate.take(max(1, maxVisibleLength - 3)) + "..."
        }
        break
    }

    return if (kept.isEmpty()) {
        normalized.take(max(1, maxVisibleLength - 3)) + "..."
    } else {
        kept.joinToString(" ") + " ..."
    }
}

fun truncateAgcTextByChars(
    text: String,
    maxVisibleChars: Int,
    longTokenBreakLength: Int = 10,
): String {
    return truncateLineToFit(text, maxVisibleChars, longTokenBreakLength)
}

fun wrapAgcTextByChars(text: String, maxCharsPerLine: Int): List<String> {
    val safeMaxChars = max(1, maxCharsPerLine)
    val wrappedLines = mutableListOf<String>()
    text.split("\n").forEach { explicitLine ->
        wrappedLines.addAll(wrapTextLineWordAware(explicitLine, safeMaxChars))
    }
    return if (wrappedLines.isEmpty()) listOf("") else wrappedLines
}

fun fitAgcTextByChars(
    text: String,
    maxCharsPerLine: Int,
    maxLines: Int,
    longTokenBreakLength: Int = 10,
): String {
    return fitAgcTextLinesByChars(
        text = text,
        maxCharsPerLine = maxCharsPerLine,
        maxLines = maxLines,
        longTokenBreakLength = longTokenBreakLength,
    ).lines.joinToString("\n")
}

private fun fitAgcTextLinesByChars(
    text: String,
    maxCharsPerLine: Int,
    maxLines: Int,
    longTokenBreakLength: Int = 10,
): FittedTextLines {
    val safeMaxChars = max(1, maxCharsPerLine)
    val safeMaxLines = max(1, maxLines)
    val wrappedLines = wrapAgcTextByChars(text, safeMaxChars)
    var wasTruncated = false
    val cappedLines = if (wrappedLines.size > safeMaxLines) {
        wasTruncated = true
        val visible = wrappedLines.take(safeMaxLines).toMutableList()
        val overflow = wrappedLines.drop(safeMaxLines - 1).joinToString(" ")
        visible[visible.lastIndex] = truncateLineToFit(overflow, safeMaxChars, longTokenBreakLength)
        visible
    } else {
        wrappedLines.map { line ->
            if (line.length > safeMaxChars) {
                wasTruncated = true
                truncateLineToFit(line, safeMaxChars, longTokenBreakLength)
            } else {
                line
            }
        }
    }
    return FittedTextLines(
        lines = cappedLines.ifEmpty { listOf("") },
        wasTruncated = wasTruncated,
    )
}

private val weakLineEndingWords = setOf(
    "a",
    "an",
    "and",
    "as",
    "at",
    "for",
    "in",
    "of",
    "or",
    "the",
    "to",
    "with",
)

/**
 * Central AGC text fitting policy. It owns the padding, line capacity, vertical
 * growth, and ellipsis behavior together so individual buttons do not each
 * rediscover slightly different wrapping and centering rules.
 */
fun computeTextFitLayout(
    text: String,
    availableWidth: Float,
    availableHeight: Float = Float.POSITIVE_INFINITY,
    minRowHeight: Float = 18f,
    horizontalPadding: Float = 8f,
    verticalPadding: Float = 8f,
    approxCharWidthPx: Float = 6.8f,
    lineHeightPx: Float = 15f,
    maxLines: Int = 3,
    canGrowHeight: Boolean = true,
    maxHeight: Float = Float.POSITIVE_INFINITY,
    longTokenBreakLength: Int = 10,
): WrappedLabelLayout {
    val safeMaxLines = max(1, maxLines)
    val effectiveTextWidth = max(8f, availableWidth - horizontalPadding)
    val maxCharsPerLine = max(1, (effectiveTextWidth / max(1f, approxCharWidthPx)).toInt())
    val heightLimitedLines = if (availableHeight.isFinite() && !canGrowHeight) {
        max(1, ((availableHeight - verticalPadding) / max(1f, lineHeightPx)).toInt())
    } else {
        safeMaxLines
    }
    val cappedMaxLines = min(safeMaxLines, heightLimitedLines)

    val fittedLines = fitAgcTextLinesByChars(
        text = text,
        maxCharsPerLine = maxCharsPerLine,
        maxLines = cappedMaxLines,
        longTokenBreakLength = longTokenBreakLength,
    )
    val cappedLines = fittedLines.lines

    val lineCount = max(1, cappedLines.size)
    val desiredHeight = max(minRowHeight, verticalPadding + lineCount * lineHeightPx)
    val rowHeight = if (canGrowHeight) {
        min(desiredHeight, maxHeight)
    } else {
        min(desiredHeight, availableHeight)
    }
    val widestLineChars = cappedLines.maxOfOrNull { it.length } ?: 0
    return WrappedLabelLayout(
        wrappedText = cappedLines.joinToString("\n"),
        lineCount = lineCount,
        rowHeight = rowHeight,
        textWidth = min(effectiveTextWidth, widestLineChars * approxCharWidthPx),
        renderHeight = rowHeight,
        wasTruncated = fittedLines.wasTruncated,
    )
}

fun computeWrappedLabelLayout(
    text: String,
    rowWidth: Float,
    minButtonHeight: Float = 18f,
    horizontalPadding: Float = 8f,
    verticalPadding: Float = 8f,
    approxCharWidthPx: Float = 6.8f,
    lineHeightPx: Float = 15f,
    maxLines: Int = 3,
): WrappedLabelLayout {
    return computeTextFitLayout(
        text = text,
        availableWidth = rowWidth,
        minRowHeight = minButtonHeight,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        approxCharWidthPx = approxCharWidthPx,
        lineHeightPx = lineHeightPx,
        maxLines = maxLines,
        canGrowHeight = true,
    )
}

fun TooltipMakerAPI.addTagLabelPara(text: String, pad: Float = 0f) {
    addAgcText(text, pad)
}
