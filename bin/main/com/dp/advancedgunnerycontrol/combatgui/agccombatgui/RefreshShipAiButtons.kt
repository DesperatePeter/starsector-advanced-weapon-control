package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.typesandvalues.defaultShipMode
import com.dp.advancedgunnerycontrol.typesandvalues.loadShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.saveShipModes
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup
import org.magiclib.combatgui.buttongroups.MagicCombatRefreshButtonsAction

class RefreshShipAiButtons(private val ship: ShipAPI) : MagicCombatRefreshButtonsAction {
    override fun refreshButtons(group: MagicCombatDataButtonGroup) {
        if (loadShipModes(ship, Values.storageIndex).isEmpty()) {
            saveShipModes(ship, Values.storageIndex, listOf(defaultShipMode))
        }
        group.refreshAllButtons(loadShipModes(ship, Values.storageIndex))
    }
}