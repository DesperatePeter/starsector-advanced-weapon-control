package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class NeverVentAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    override fun blockCommands(): List<ShipCommand> {
        return listOf(ShipCommand.VENT_FLUX)
    }
}