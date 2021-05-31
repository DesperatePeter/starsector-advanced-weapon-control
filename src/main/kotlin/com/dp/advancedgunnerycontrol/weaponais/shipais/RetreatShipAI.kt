package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.mission.FleetSide

class RetreatShipAI (baseAI: ShipAIPlugin, ship: ShipAPI, private val hullThreshold : Float) : CustomShipAI(baseAI, ship) {
    private var fleetManagerAPI : CombatFleetManagerAPI? = null

    init {
        fleetManagerAPI = Global.getCombatEngine()?.getFleetManager(FleetSide.PLAYER)
    }
    override fun advanceImpl(p0: Float) {
        if(ship.hullLevel <= hullThreshold){
            val taskMan = fleetManagerAPI?.getTaskManager(false) ?: return
            fleetManagerAPI?.let {
                if (taskMan.getAssignmentFor(ship)?.type == CombatAssignmentType.RETREAT) return
                taskMan.orderRetreat(it.getDeployedFleetMember(ship), true, false)
            }
        }
    }
}