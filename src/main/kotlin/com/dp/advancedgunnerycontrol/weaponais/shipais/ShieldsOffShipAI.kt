package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class ShieldsOffShipAI(ship: ShipAPI, private val fluxThreshold : Float) : ShipCommandGenerator(ship) {
    override fun generateCommands(): List<ShipCommandWrapper> {
        if(ship.shield?.isOn == true && ship.fluxLevel >= fluxThreshold){
            return listOf(ShipCommandWrapper(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK))
        }
        return emptyList()
    }
}