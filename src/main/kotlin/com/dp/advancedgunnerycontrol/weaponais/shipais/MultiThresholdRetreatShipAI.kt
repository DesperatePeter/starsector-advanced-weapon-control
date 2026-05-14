package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatAssignmentType
import com.fs.starfarer.api.combat.CombatFleetManagerAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.mission.FleetSide

enum class RetreatThresholdMetric {
    CR,
    HULL
}

class MultiThresholdRetreatShipAI(
    ship: ShipAPI,
    thresholds: List<Float>,
    private val metric: RetreatThresholdMetric,
    private val directRetreatOverride: Boolean? = null,
) : ShipCommandGenerator(ship) {
    private val thresholds = thresholds
        .map { it.coerceIn(0f, 1f) }
        .distinct()
        .sortedDescending()
    private val triggeredThresholds = mutableSetOf<Float>()
    private val fleetManagerAPI: CombatFleetManagerAPI? = Global.getCombatEngine()?.getFleetManager(FleetSide.PLAYER)

    override fun generateCommands(): List<ShipCommandWrapper> {
        thresholds.firstOrNull { currentLevel() <= it && triggeredThresholds.add(it) } ?: return emptyList()
        val taskMan = fleetManagerAPI?.getTaskManager(false) ?: return emptyList()
        if (taskMan.getAssignmentFor(ship)?.type == CombatAssignmentType.RETREAT) return emptyList()
        fleetManagerAPI?.let {
            taskMan.orderRetreat(it.getDeployedFleetMember(ship), true, directRetreatOverride ?: Settings.directRetreat())
        }
        return emptyList()
    }

    private fun currentLevel(): Float =
        when (metric) {
            RetreatThresholdMetric.CR -> ship.currentCR
            RetreatThresholdMetric.HULL -> ship.hullLevel
        }
}
