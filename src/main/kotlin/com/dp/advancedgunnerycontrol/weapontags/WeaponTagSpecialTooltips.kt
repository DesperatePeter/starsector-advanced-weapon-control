package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import kotlin.math.roundToInt

internal fun noPdHealthTooltip(canonicalTag: String): String {
    return "Does not target fighters or missiles when this weapon estimates their health is below ${
        extractRawNumericThreshold(noPdHealthRegex, canonicalTag).toInt()
    }. Poor damage matchups count armor or shields as tougher; strong matchups do not lower the estimate."
}

internal fun noPdWasteTooltip(canonicalTag: String): String {
    val wastePercent = extractRawNumericThreshold(noPdWasteRegex, canonicalTag).toInt()
    val cleanupCap = extractOptionalRawRegexSecondValue(noPdWasteRegex, canonicalTag)
        ?: Settings.noPDWasteCleanupDamageCap()
    val examplePacketDamage = 500
    val exampleMinimumTargetHealth = (examplePacketDamage * (1f - wastePercent / 100f)).roundToInt()
    return "Does not target fighters or missiles when this weapon would waste more than $wastePercent% of its estimated attack-packet damage. " +
        "For example, with a $examplePacketDamage damage packet, it refuses targets that need less than $exampleMinimumTargetHealth damage. " +
        "Weapons with attack-packet damage at or below ${formatTooltipNumber(cleanupCap)} ignore this waste check, so low-damage PD can still clean up weak targets."
}

internal fun lowRofTooltip(canonicalTag: String): String {
    val factorPercent = extractRawNumericThreshold(rofRegex, canonicalTag).toInt().coerceAtLeast(1)
    val shotsPerSecond = 100f / factorPercent.toFloat()
    val secondsPerShot = factorPercent.toFloat() / 100f
    return "Multiplies the weapon's normal firing delay by $factorPercent%. " +
        "A weapon that normally fires once per second would fire ${formatTooltipNumber(shotsPerSecond)} times per second instead, or once every ${formatTooltipNumber(secondsPerShot)} seconds."
}

internal fun priorityMultiplierTooltip(targetText: String, regex: Regex, canonicalTag: String): String {
    return "Prioritizes $targetText when present.\nIncreases priority by a factor of ${
        extractRawNumericThreshold(regex, canonicalTag).toInt()
    }.\nCombine multiple Prio-tags to de-prioritize everything else."
}
