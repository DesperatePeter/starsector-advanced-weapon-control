package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class SpamSystemAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    override fun generateCommands(): List<ShipCommandWrapper> {
        return if (ship.system?.isActive == false) listOf(
            ShipCommandWrapper(ShipCommand.USE_SYSTEM)
        ) else listOf()
    }
}