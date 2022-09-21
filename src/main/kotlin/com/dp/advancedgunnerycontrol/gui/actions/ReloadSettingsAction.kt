package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView

class ReloadSettingsAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        Settings.loadSettings()
        attributes.tagView = TagListView()
    }

    override fun getTooltip(): String = "Reload settings from Settings.editme file without having to restart the game"

    override fun getName(): String = "Reload Settings"
}