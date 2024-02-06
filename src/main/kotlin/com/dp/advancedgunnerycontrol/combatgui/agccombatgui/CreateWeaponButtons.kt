package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip
import org.magiclib.combatgui.buttongroups.MagicCombatCreateButtonsAction
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup

class CreateWeaponButtons(private val tagListView: TagListView) : MagicCombatCreateButtonsAction {
    override fun createButtons(group: MagicCombatDataButtonGroup) {
        tagListView.view().forEach {
            group.addButton(it, it, getTagTooltip(it))
        }
    }
}