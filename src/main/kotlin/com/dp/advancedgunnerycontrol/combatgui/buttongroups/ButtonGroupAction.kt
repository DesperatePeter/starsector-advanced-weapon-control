package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * implement this interface to tell a button group what action to perform
 */
interface ButtonGroupAction {
    /**
     * @param data list of data of all currently active buttons (maybe empty)
     * @param triggeringButtonData data of the button that was clicked (null if button was deselected)
     */
    fun execute(data: List<Any>, triggeringButtonData: Any?, deselectedButtonData: Any? = null)

    /**
     * Override this function to perform some kind of action when a button of the group is hovered over
     */
    fun onHover(){}
}