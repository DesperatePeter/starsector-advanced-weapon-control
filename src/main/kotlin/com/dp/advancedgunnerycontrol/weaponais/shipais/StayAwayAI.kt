package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.weaponais.determineUniversalShipTarget
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import org.lazywizard.lazylib.ext.minus

class StayAwayAI(ship: ShipAPI) : ShipCommandGenerator(ship) {

    companion object{
        const val SCANNING_RANGE = 2500f
    }
    override fun generateCommands(): List<ShipCommandWrapper> {
        return if (shouldBackOff()){
            listOf(ShipCommandWrapper(ShipCommand.ACCELERATE_BACKWARDS))
        }else{
            emptyList()
        }
    }

    override fun blockCommands(): List<ShipCommand> {
        return if (shouldBackOff()){
            listOf(ShipCommand.ACCELERATE)
        }else{
            emptyList()
        }
    }

    private fun shouldBackOff(): Boolean{
        val shipTargetDistance = ship.determineUniversalShipTarget()?.location?.minus(ship.location)?.length()
            ?: return false
        return shipTargetDistance < SCANNING_RANGE
    }
}