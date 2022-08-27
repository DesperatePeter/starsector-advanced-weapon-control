package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * Sample implementation of CreateButtonsAction interface that creates
 * a button for each entry in names. Display text, name and tooltip will be
 * equal to that entry.
 */
class CreateSimpleButtons(private val names: List<String>) : CreateButtonsAction {
    override fun createButtons(group: DataButtonGroup) {
        names.forEach {
            group.addButton(it, it, it, false)
        }
    }
}