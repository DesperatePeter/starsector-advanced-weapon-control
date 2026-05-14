package com.dp.advancedgunnerycontrol.gui.actions


import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings

class ToggleSimpleAdvancedAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.clearCustomListModalState()
        attributes.tagView.reset()
        Settings.isAdvancedMode = !Settings.isAdvancedMode
    }

    override fun getTooltip(): String {
        if(Settings.isAdvancedMode) return "Switch to simple mode. Simple mode shows a smaller set of weapon tags and ship modes."
        return "Switch to advanced mode. Advanced mode shows ship AI modes and all weapon tags."
    }

    override fun getName(): String = if(Settings.isAdvancedMode) "Switch to simple mode" else "Switch to advanced mode"
}
