package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage

class CopyLoadoutAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        affectedShips().map { it.id }.forEach { shipId ->
            ShipModeStorage[AGCGUI.storageIndex].modesByShip[shipId] =
                (ShipModeStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
            Settings.tagStorage[AGCGUI.storageIndex].modesByShip[shipId] =
                (Settings.tagStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
        }
    }

    override fun getTooltip(): String {
        return "Copy loadout #${lastIndex() + 1} into current loadout." +
                "\n$fleetBoilerplateText"
    }

    override fun getName(): String = "Copy previous loadout"
}