package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * Implement this interface to create buttons for a button group
 */
interface CreateButtonsAction {
    /**
     * call group.addButton in this method to add a button to the group
     * Example:
     * void createButtons(DataButtonGroup group){
     *   group.addButton("Button1", "Button1Data", "This text will be displayed when hovering over Button1");
     * }
     */
    fun createButtons(group: DataButtonGroup)
}