package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.CreateButtonsAction
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.DataButtonGroup
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip

class CreateWeaponButtons(private val tagListView: TagListView) : CreateButtonsAction {
    override fun createButtons(group: DataButtonGroup) {
        tagListView.view().forEach {
            group.addButton(it, it, getTagTooltip(it))
        }
    }
}