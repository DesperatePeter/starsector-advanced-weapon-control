package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.CreateButtonsAction
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip

class CreateWeaponButtons() : CreateButtonsAction {
    override fun createButtons(group: DataButtonGroup) {
        Settings.tagList().forEach {
            group.addButton(it, it, getTagTooltip(it))
        }
    }
}