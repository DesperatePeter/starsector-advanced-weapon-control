package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.detailedShipModeDescriptions
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeFromString
import com.dp.advancedgunnerycontrol.typesandvalues.shipModeToString
import org.magiclib.combatgui.buttongroups.MagicCombatCreateButtonsAction
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup

class CreateShipAiButtons : MagicCombatCreateButtonsAction {
    override fun createButtons(group: MagicCombatDataButtonGroup) {
        if(!Settings.isAdvancedMode) return
        Settings.getCurrentShipModes().mapNotNull { shipModeToString[it] }.forEach {
            group.addButton(it, it, detailedShipModeDescriptions[shipModeFromString[it]] ?: "")
        }
    }
}