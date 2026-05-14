package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.settings.Settings

internal fun parseCanonicalFluxCondition(
    tag: String,
    regex: Regex,
    metric: FluxMetric,
    comparator: FluxComparator,
    requireSoftFluxCap: Boolean = false,
    totalFluxCapExtractor: (Regex, String) -> Float? = ::extractOptionalTotalFluxCapFraction,
): FluxCondition? {
    if (!regex.matches(tag)) return null
    return FluxCondition(
        metric = metric,
        comparator = comparator,
        threshold = extractPercentThresholdFraction(regex, tag),
        requireTotalFluxBelowSoftFluxCap = requireSoftFluxCap,
        totalFluxCap = totalFluxCapExtractor(regex, tag)
    )
}

internal fun fluxConditionWording(condition: FluxCondition): String {
    val metric = when (condition.metric) {
        FluxMetric.TOTAL -> "total flux"
        FluxMetric.SOFT -> "soft flux"
        FluxMetric.HARD -> "hard flux"
    }
    val comparator = when (condition.comparator) {
        FluxComparator.GREATER_THAN -> "greater than"
        FluxComparator.LESS_THAN -> "below"
        FluxComparator.GREATER_OR_EQUAL -> "greater than or equal to"
        FluxComparator.LESS_OR_EQUAL -> "below or equal to"
    }
    val threshold = "${thresholdAsPercent(condition.threshold)}%"
    val base = "$metric is $comparator $threshold"
    val cap = condition.totalFluxCap
        ?: Settings.softFluxTotalFluxCap().takeIf { condition.requireTotalFluxBelowSoftFluxCap }
        ?: return base
    return "$base and total flux is below ${thresholdAsPercent(cap)}%"
}

internal fun fluxConditionThresholdPercent(condition: FluxCondition): String = "${thresholdAsPercent(condition.threshold)}%"

internal fun pdTargetingRestrictionTooltip(): String = "Restricts targeting to fighters and missiles."

internal fun tooltipWithActivationCondition(base: String, condition: String): String {
    return "$base\nActivation condition: $condition."
}

internal fun holdFireTooltip(
    tag: String,
    regex: Regex,
    metric: FluxMetric,
    comparator: FluxComparator,
    requireSoftFluxCap: Boolean = false,
): String {
    return "Fires only while ${
        fluxConditionWording(parseCanonicalFluxCondition(tag, regex, metric, comparator, requireSoftFluxCap)!!)
    }."
}

internal fun forceFireTooltip(
    tag: String,
    regex: Regex,
    metric: FluxMetric,
    comparator: FluxComparator,
    requireSoftFluxCap: Boolean = false,
): String {
    return "ForceFire: ignores firing restrictions from other tags while ${
        fluxConditionWording(parseCanonicalFluxCondition(tag, regex, metric, comparator, requireSoftFluxCap)!!)
    }.\nNote: this bypasses firing restrictions, not targeting restrictions."
}
