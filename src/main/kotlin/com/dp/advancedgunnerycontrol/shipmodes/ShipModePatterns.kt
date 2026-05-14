package com.dp.advancedgunnerycontrol.shipmodes

import java.util.Locale

internal val ventModeRegex = Regex("""^Vent\(TF>(\d{1,3})%(?:,S=(\d+(?:\.\d{1,2})?))?(?:,A=([TF]))?\)$""")
internal val aggressiveVentModeRegex = Regex("""^VentA\(TF>(\d{1,3})%\)$""")
internal val compactAggressiveVentModeRegex = Regex("""^VntA\((?:Flx|Flux)>(\d{1,3})%\)$""")
internal val retreatModeRegex = Regex("""^Run\(HP<(\d{1,3})%\)$""")
internal val singleCrRetreatModeRegex = Regex("""^RunCR\(CR<(\d{1,3})%(?:,DR=([TF]))?\)$""")
internal val singleHullRetreatModeRegex = Regex("""^RunHP\(HP<(\d{1,3})%(?:,DR=([TF]))?\)$""")
internal val crRetreatModeRegex = Regex("""^RunCR\(CR<((?:\d{1,3}|-)/(?:\d{1,3}|-)/(?:\d{1,3}|-))%(?:,DR=([TF]))?\)$""")
internal val hullRetreatModeRegex = Regex("""^RunHP\(HP<((?:\d{1,3}|-)/(?:\d{1,3}|-)/(?:\d{1,3}|-))%(?:,DR=([TF]))?\)$""")
internal val lowShieldModeRegex = Regex("""^LowShield\(TF>(\d{1,3})%\)$""")
internal val shieldUpModeRegex = Regex("""^ShieldUp\(TF<(\d{1,3})%\)$""")
internal val shieldUpPlusModeRegex = Regex("""^ShieldUp\+\(TF<(\d{1,3})%\)$""")
internal val personalityModeRegex = Regex("""^Personality\(([^)]+)\)$""", RegexOption.IGNORE_CASE)

internal fun formatVentSafetyFactor(value: Float): String {
    var rendered = String.format(Locale.US, "%.2f", value)
    while (rendered.endsWith("0") && rendered.substringAfter('.').length > 1) {
        rendered = rendered.dropLast(1)
    }
    return rendered
}

private val personalityIdToLabel = linkedMapOf(
    "timid" to "Timid",
    "cautious" to "Cautious",
    "steady" to "Steady",
    "aggressive" to "Aggressive",
    "reckless" to "Reckless",
)

fun shipModePersonalityLabel(personalityId: String): String? =
    personalityIdToLabel[personalityId.trim().lowercase()]
