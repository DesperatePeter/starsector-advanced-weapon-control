package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class ShieldsOffShipAI(baseAI: ShipAIPlugin, ship: ShipAPI) : CustomShipAI(baseAI, ship) {
    companion object{
        const val fluxThreshold = 0.5f
    }
    override fun advanceImpl(p0: Float) {
        if(ship.shield?.isOn == true && ship.fluxLevel >= fluxThreshold){
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0)
        }
    }
}