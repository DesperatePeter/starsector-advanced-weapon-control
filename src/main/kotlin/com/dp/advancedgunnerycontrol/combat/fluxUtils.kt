package com.dp.advancedgunnerycontrol.combat

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.ShipAPI

enum class FluxMetric {
    TOTAL,
    SOFT,
    HARD
}

enum class FluxComparator {
    GREATER_THAN,
    LESS_THAN,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL
}

data class FluxCondition(
    val metric: FluxMetric,
    val comparator: FluxComparator,
    val threshold: Float,
    val requireTotalFluxBelowSoftFluxCap: Boolean = false,
    val totalFluxCap: Float? = null,
)

fun ShipAPI.softFluxLevel(): Float = (fluxLevel - hardFluxLevel).coerceIn(0f, 1f)

private fun ShipAPI.fluxValue(metric: FluxMetric): Float {
    return when (metric) {
        FluxMetric.TOTAL -> fluxLevel
        FluxMetric.SOFT -> softFluxLevel()
        FluxMetric.HARD -> hardFluxLevel
    }
}

fun ShipAPI.meetsFluxCondition(condition: FluxCondition): Boolean {
    val totalFluxCap = condition.totalFluxCap
        ?: Settings.softFluxTotalFluxCap().takeIf { condition.requireTotalFluxBelowSoftFluxCap }
    if (totalFluxCap != null && !totalFluxBelowThreshold(totalFluxCap)) {
        return false
    }
    val value = fluxValue(condition.metric)
    return when (condition.comparator) {
        FluxComparator.GREATER_THAN -> value > condition.threshold
        FluxComparator.LESS_THAN -> value < condition.threshold
        FluxComparator.GREATER_OR_EQUAL -> value >= condition.threshold
        FluxComparator.LESS_OR_EQUAL -> value <= condition.threshold
    }
}

fun ShipAPI.totalFluxAtOrBelowThreshold(totalFluxLimit: Float): Boolean = fluxLevel <= totalFluxLimit

fun ShipAPI.totalFluxBelowThreshold(totalFluxLimit: Float): Boolean = fluxLevel < totalFluxLimit

fun ShipAPI.totalFluxAboveThreshold(totalFluxLimit: Float): Boolean = fluxLevel > totalFluxLimit

fun ShipAPI.softFluxBelowThresholdAndTotalFluxBelowCap(softFluxLimit: Float): Boolean {
    return meetsFluxCondition(
        FluxCondition(
            metric = FluxMetric.SOFT,
            comparator = FluxComparator.LESS_THAN,
            threshold = softFluxLimit,
            requireTotalFluxBelowSoftFluxCap = true
        )
    )
}

fun ShipAPI.softFluxAboveThresholdAndTotalFluxBelowCap(softFluxLimit: Float): Boolean {
    return meetsFluxCondition(
        FluxCondition(
            metric = FluxMetric.SOFT,
            comparator = FluxComparator.GREATER_THAN,
            threshold = softFluxLimit,
            requireTotalFluxBelowSoftFluxCap = true
        )
    )
}

fun ShipAPI.softFluxBelowThreshold(softFluxLimit: Float): Boolean {
    return softFluxBelowThresholdAndTotalFluxBelowCap(softFluxLimit)
}
