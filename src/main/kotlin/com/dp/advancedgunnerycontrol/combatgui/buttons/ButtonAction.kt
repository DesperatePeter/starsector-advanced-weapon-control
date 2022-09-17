package com.dp.advancedgunnerycontrol.combatgui.buttons

/**
 * Implement this and override execute to instruct buttons what they should do when clicked
 */
interface ButtonAction {
    /**
     * Will get executed when button is clicked
     */
    fun execute()
}