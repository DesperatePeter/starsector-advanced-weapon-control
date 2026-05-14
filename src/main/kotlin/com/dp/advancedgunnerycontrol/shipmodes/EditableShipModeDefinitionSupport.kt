package com.dp.advancedgunnerycontrol.shipmodes

import java.util.Locale
import kotlin.math.roundToInt

internal const val SHIP_MODE_PARAM_THRESHOLD = "threshold"
internal const val SHIP_MODE_PARAM_PERSONALITY = "personality"
internal const val SHIP_MODE_PARAM_DIRECT_RETREAT = "directRetreat"
internal const val SHIP_MODE_PARAM_VENT_SAFETY_FACTOR = "safetyFactor"
internal const val SHIP_MODE_PARAM_VENT_AGGRESSIVE = "aggressive"
internal const val SHIP_MODE_PARAM_MULTI_ENABLED_PREFIX = "enabledThreshold"
internal const val SHIP_MODE_PARAM_MULTI_THRESHOLD_PREFIX = "threshold"

internal val multiThresholdRetreatDefaults = listOf(75, 50, 25)

internal fun settingsPercent(value: Float): Int = (value * 100f).roundToInt().coerceIn(0, 100)

internal fun enabledParameterId(index: Int): String =
    "$SHIP_MODE_PARAM_MULTI_ENABLED_PREFIX${index + 1}"

internal fun thresholdParameterId(index: Int): String =
    "$SHIP_MODE_PARAM_MULTI_THRESHOLD_PREFIX${index + 1}"

internal fun formatVentParameter(value: Float): String {
    var rendered = String.format(Locale.US, "%.2f", value)
    while (rendered.endsWith("0") && rendered.substringAfter('.').length > 1) {
        rendered = rendered.dropLast(1)
    }
    return rendered
}
