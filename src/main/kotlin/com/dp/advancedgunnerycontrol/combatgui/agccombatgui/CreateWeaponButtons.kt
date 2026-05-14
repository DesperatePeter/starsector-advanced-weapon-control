package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.weapontags.getTagTooltip
import org.magiclib.combatgui.buttongroups.MagicCombatCreateButtonsAction
import org.magiclib.combatgui.buttongroups.MagicCombatDataButtonGroup

class CreateWeaponButtons(private val tagListView: TagListView) : MagicCombatCreateButtonsAction {
    override fun createButtons(group: MagicCombatDataButtonGroup) {
        tagListView.view().forEach {
            group.addButton(it, it, getTagTooltip(it))
        }
    }
}
