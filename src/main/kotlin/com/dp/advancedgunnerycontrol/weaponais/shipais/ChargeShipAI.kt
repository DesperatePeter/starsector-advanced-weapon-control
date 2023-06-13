package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import org.lazywizard.lazylib.ext.minus

class ChargeShipAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    override fun generateCommands(): List<ShipCommandWrapper> {
        if(areWeaponsNotInRange()){
            return listOf(ShipCommandWrapper(ShipCommand.ACCELERATE))
        }
        return emptyList()
    }

    override fun blockCommands(): List<ShipCommand> {
        if (areWeaponsNotInRange()){
            return listOf(ShipCommand.DECELERATE, ShipCommand.ACCELERATE_BACKWARDS)
        }
        return emptyList()
    }

    private fun areWeaponsNotInRange(): Boolean{
        ship.shipTarget?.let { tgt ->
            if(tgt.owner == ship.owner) return false
            return ship.allWeapons.any {
                it.range < (it.location - tgt.location).length()
            }
        }
        return false
    }
}