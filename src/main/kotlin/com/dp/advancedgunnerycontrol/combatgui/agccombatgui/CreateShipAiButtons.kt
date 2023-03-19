package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.CreateButtonsAction
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.typesandvalues.ShipModes
import com.dp.advancedgunnerycontrol.typesandvalues.detailedShipModeDescriptions
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeFromString
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeToString

class CreateShipAiButtons : CreateButtonsAction {
    override fun createButtons(group: DataButtonGroup) {
        ShipModes.values().mapNotNull { shipModeToString[it] }.forEach {
            group.addButton(it, it, detailedShipModeDescriptions[shipModeFromString[it]] ?: "")
        }
    }
}