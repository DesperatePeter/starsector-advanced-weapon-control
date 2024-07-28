package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings

class ToggleSimpleAdvancedAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.tagView.reset()
        Settings.isAdvancedMode = !Settings.isAdvancedMode
    }

    override fun getTooltip(): String {
        if(Settings.isAdvancedMode) return "Simple mode hides ship AI modes and more advanced tags"
        return "Show all options"
    }

    override fun getName(): String = if(Settings.isAdvancedMode) "Switch to simple mode" else "Switch to advanced mode"
}