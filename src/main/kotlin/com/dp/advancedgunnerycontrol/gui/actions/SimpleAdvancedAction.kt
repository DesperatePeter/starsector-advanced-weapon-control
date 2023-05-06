package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings

class SimpleAdvancedAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        Settings.isAdvancedMode = !Settings.isAdvancedMode
    }

    override fun getTooltip(): String {
        if(Settings.isAdvancedMode) return "Simple mode hides ship AI modes and more advanced tags"
        return "Show all options"
    }

    override fun getName(): String = if(Settings.isAdvancedMode) "Switch to simple mode" else "Switch to advanced mode"
}