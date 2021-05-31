package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.mission.FleetSide

class RetreatShipAI (ship: ShipAPI, private val hullThreshold : Float) : ShipCommandGenerator(ship) {
    private var fleetManagerAPI : CombatFleetManagerAPI? = null

    init {
        fleetManagerAPI = Global.getCombatEngine()?.getFleetManager(FleetSide.PLAYER)
    }

    override fun generateCommands(): List<ShipCommandWrapper> {
        if(ship.hullLevel <= hullThreshold){
            val taskMan = fleetManagerAPI?.getTaskManager(false) ?: return emptyList()
            fleetManagerAPI?.let {
                if (taskMan.getAssignmentFor(ship)?.type == CombatAssignmentType.RETREAT) return emptyList()
                taskMan.orderRetreat(it.getDeployedFleetMember(ship), true, Settings.directRetreat())
            }
        }
        return emptyList()
    }
}