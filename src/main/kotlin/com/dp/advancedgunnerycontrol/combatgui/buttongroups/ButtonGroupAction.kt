package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * implement this interface to tell a button group what action to perform
 */
interface ButtonGroupAction {
    /**
     * this method will get called when a button in this button group gets clicked by the user
     *
     * @param data list of data of all currently active buttons (maybe empty)
     * @param selectedButtonData data of the button that was clicked if it was selected (null if button was deselected)
     * @param deselectedButtonData data of the button that was clicked if it was deselected (null if button was selected)
     */
    fun execute(data: List<Any>, selectedButtonData: Any?, deselectedButtonData: Any? = null)

    /**
     * Override this function to perform some kind of action when a button of the group is hovered over
     */
    fun onHover(){}
}