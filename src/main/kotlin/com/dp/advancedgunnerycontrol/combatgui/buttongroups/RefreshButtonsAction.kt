package com.dp.advancedgunnerycontrol.combatgui.buttongroups

/**
 * defines an action to be performed every frame on a button group
 * Use this to e.g. disabled buttons that are incompatible with other active buttons
 */
interface RefreshButtonsAction {
    /**
     * will get called every frame
     * @param group to perform actions on. Use group.getActiveButtonData() and group.buttons to interact with buttons
     */
    fun refreshButtons(group: DataButtonGroup)
}