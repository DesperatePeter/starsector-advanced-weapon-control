package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.RefreshButtonsAction
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.fs.starfarer.api.combat.ShipAPI

class RefreshShipAiButtons(private val ship: ShipAPI) : RefreshButtonsAction {
    override fun refreshButtons(group: DataButtonGroup) {
        if(loadShipModes(ship, Values.storageIndex).isEmpty()){
            saveShipModes(ship, Values.storageIndex, listOf(defaultShipMode))
        }
        group.refreshAllButtons(loadShipModes(ship, Values.storageIndex))
    }
}