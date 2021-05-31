package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class AutofireShipAI(ship: ShipAPI) : ShipCommandGenerator(ship) {

    override fun generateCommands(): List<ShipCommandWrapper> {
        return listOf(ShipCommandWrapper(ShipCommand.SELECT_GROUP, null, (ship.weaponGroupsCopy?.size ?: 0))) +
                ship.weaponGroupsCopy.mapIndexed { index, group ->
                    if (!group.isAutofiring) {
                        ShipCommandWrapper(ShipCommand.TOGGLE_AUTOFIRE, null, index)
                    } else {
                        null
                    }
                }.filterNotNull()
    }
}