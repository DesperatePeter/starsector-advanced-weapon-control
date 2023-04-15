package com.dp.advancedgunnerycontrol.combatgui.buttons

/**
 * Simple button that does something (defined by button action) when clicked
 * If possible, use GuiBase.addButton rather than using this directly
 */
class ActionButton(
    private val action: ButtonAction? = null, info: ButtonInfo
) : ButtonBase(info) {
    override fun advance(): Boolean {
        if (isClicked()) {
            action?.run { execute() }
            isActive = true
            return true
        }
        isActive = false
        return false
    }
}