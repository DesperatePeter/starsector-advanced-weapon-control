package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipmodes.shipModeDescription
import org.magiclib.combatgui.buttongroups.MagicCombatCreateButtonsAction
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup

class CreateShipAiButtons : MagicCombatCreateButtonsAction {
    override fun createButtons(group: MagicCombatDataButtonGroup) {
        Settings.getCurrentShipModeNames().forEach {
            group.addButton(it, it, shipModeDescription(it))
        }
    }
}
