package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class VentShipAI(baseAI: ShipAIPlugin, ship: ShipAPI) : CustomShipAI(baseAI, ship) {
    companion object{
        const val fluxThreshold = 0.5f
    }
    override fun advanceImpl(p0: Float) {
        if(ship.fluxLevel >= fluxThreshold){
            ship.giveCommand(ShipCommand.VENT_FLUX, null, 0)
        }
    }
}