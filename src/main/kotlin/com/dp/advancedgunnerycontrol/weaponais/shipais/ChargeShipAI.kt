package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.utils.hasAgcTag
import com.dp.advancedgunnerycontrol.utils.hasAnyAgcTag
import com.dp.advancedgunnerycontrol.weaponais.determineUniversalShipTarget
import com.dp.advancedgunnerycontrol.weaponais.isPD
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.minus

class ChargeShipAI(ship: ShipAPI) : ShipCommandGenerator(ship) {

    private var relevantWeapons: List<WeaponAPI> = emptyList()
        get() { return field.ifEmpty {
            kotlin.run {
                field = ship.allWeapons.filter {
                    !isPD(it) || it.hasAnyAgcTag("NoPD", "NoMissiles", "PrioShips")
                }
                field
            }
        }
        }
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

        ship.determineUniversalShipTarget()?.let { tgt ->
            if(tgt.owner == ship.owner || tgt.owner == 100) return false
            return relevantWeapons.any {
                it.range < (it.location - tgt.location).length()
            }
        }
        return false
    }
}