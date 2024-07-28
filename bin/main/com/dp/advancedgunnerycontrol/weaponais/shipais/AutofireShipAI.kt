package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class AutofireShipAI(ship: ShipAPI) : ShipCommandGenerator(ship) {

    override fun blockCommands(): List<ShipCommand> {
        val isEverythingOnAutofire = ship.weaponGroupsCopy.all { it.isAutofiring }
        val isEmptyGroupSelected = ship.selectedGroupAPI == null || ship.selectedGroupAPI.weaponsCopy?.isEmpty() != false
        val toReturn = mutableListOf<ShipCommand>()
        if(isEmptyGroupSelected) toReturn += ShipCommand.SELECT_GROUP
        if (isEverythingOnAutofire) toReturn += ShipCommand.TOGGLE_AUTOFIRE
        return toReturn
    }

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