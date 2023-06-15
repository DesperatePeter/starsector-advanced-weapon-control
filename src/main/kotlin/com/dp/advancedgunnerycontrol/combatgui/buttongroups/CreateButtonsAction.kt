package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * Implement this interface to create buttons for a button group
 * Consider using CreateSimpleButtons rather than creating your own action
 */
interface CreateButtonsAction {
    /**
     * call group.addButton in this method to add a button to the group
     */
    fun createButtons(group: DataButtonGroup)
}