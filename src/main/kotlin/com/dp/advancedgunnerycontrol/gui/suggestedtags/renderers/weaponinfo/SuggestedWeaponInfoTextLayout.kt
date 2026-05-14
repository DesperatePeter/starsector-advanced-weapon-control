package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import kotlin.math.max

internal object SuggestedWeaponInfoTextLayout {
    fun wrappedLineCount(line: String, width: Float): Int {
        return wrappedInfoLines(line, width).size
    }

    fun wrappedInfoLines(line: String, width: Float): List<String> {
        val maxChars = maxInfoChars(width)
        if (line.length <= maxChars) return listOf(line)
        val firstBreak = bestWrapIndex(line, maxChars)
        val firstLine = line.take(firstBreak).trimEnd()
        val remainder = line.drop(firstBreak).trimStart()
        if (remainder.isBlank()) return listOf(firstLine)
        return listOf(firstLine, fitInfoLine(remainder, width))
    }

    private fun bestWrapIndex(line: String, maxChars: Int): Int {
        val lastSpace = line.take(maxChars + 1).lastIndexOf(' ')
        return if (lastSpace >= maxChars / 2) lastSpace else maxChars
    }

    private fun maxInfoChars(width: Float): Int {
        return max(8, (width / SuggestedWeaponInfoLayout.WEAPON_INFO_APPROX_CHAR_WIDTH).toInt())
    }

    private fun fitInfoLine(line: String, width: Float): String {
        val maxChars = maxInfoChars(width)
        if (line.length <= maxChars) return line
        return line.take(max(1, maxChars - 3)).trimEnd() + "..."
    }
}
