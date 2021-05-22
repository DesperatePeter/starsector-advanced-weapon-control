package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand

class AutofireShipAI(baseAI: ShipAIPlugin, ship: ShipAPI) : CustomShipAI(baseAI, ship) {
    override fun advanceImpl(p0: Float) {
        forceAutofireGroups()
    }

    private fun forceAutofireGroups(){
        // deselect weapon groups
        ship.giveCommand(ShipCommand.SELECT_GROUP, null, (ship.weaponGroupsCopy?.size ?: 0))
        ship.weaponGroupsCopy.forEachIndexed { index, group ->
            if(!group.isAutofiring){
                ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, index)
            }
        }
    }
}