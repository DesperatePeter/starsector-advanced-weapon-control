package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.session.ShipViewHotTagCache
import com.dp.advancedgunnerycontrol.settings.Settings

class ReloadSettingsAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        Settings.loadSettings()
        AGCGUI.storageIndex = Values.storageIndex
        ShipViewHotTagCache.invalidate()
        attributes.tagView = TagListView()
    }

    override fun getTooltip(): String = "Reload Settings.editme from disk without restarting the game. This does not write to or override LunaSettings; LunaSettings values stay controlled by LunaLib and are already hotloaded automatically."

    override fun requiresConfirmation(): Boolean = true

    override fun getName(): String = "Reload Settings.EDITME"
}
