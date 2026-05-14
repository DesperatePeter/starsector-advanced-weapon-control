package com.dp.advancedgunnerycontrol.weapontags

import kotlin.math.roundToInt

internal fun thresholdAsPercent(threshold: Float): Int = (threshold.coerceIn(0f, 1f) * 100f).roundToInt()

internal fun formatTooltipNumber(value: Float): String {
    val rounded = (value * 100f).roundToInt() / 100f
    val asInt = rounded.roundToInt()
    if (kotlin.math.abs(rounded - asInt.toFloat()) < 0.001f) return asInt.toString()
    val oneDecimal = (rounded * 10f).roundToInt() / 10f
    if (kotlin.math.abs(rounded - oneDecimal) < 0.001f) return oneDecimal.toString()
    return rounded.toString()
}
