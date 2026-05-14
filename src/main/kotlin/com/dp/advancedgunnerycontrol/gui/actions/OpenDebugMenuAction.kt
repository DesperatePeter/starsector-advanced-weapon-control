package com.dp.advancedgunnerycontrol.gui.actions


import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes

class OpenDebugMenuAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.customListModalMode = CustomListModalMode.DEBUG_COLORS
    }

    override fun getTooltip(): String {
        return "Open the in-session colour debugging tool. Changes apply until the game is restarted."
    }

    override fun getName(): String = "Debug menu"

    fun isVisible(): Boolean {
        val (allLoadouts, wholeFleet) = modifierKeys()
        return allLoadouts && wholeFleet
    }
}
