package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * Implement this interface to create buttons for a button group
 */
interface CreateButtonsAction {
    /**
     * call group.addButton in this method to add a button to the group
     * @sample com.dp.advancedgunnerycontrol.combatgui.buttongroups.CreateSimpleButtons.createButtons
     */
    fun createButtons(group: DataButtonGroup)
}