package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * implement this interface to tell a button group what action to perform
 */
interface ButtonGroupAction {
    /**
     * @param data list of data of all currently active buttons
     * @param triggeringButtonData data of the button that was clicked
     */
    fun execute(data : List<Any>, triggeringButtonData: Any?)
}