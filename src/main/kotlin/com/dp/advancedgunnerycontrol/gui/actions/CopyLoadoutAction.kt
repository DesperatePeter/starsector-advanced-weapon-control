package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.dp.advancedgunnerycontrol.utils.loadPersistentTags
import com.dp.advancedgunnerycontrol.utils.persistTags

class CopyLoadoutAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        affectedShips().forEach { ship ->
            ShipModeStorage[AGCGUI.storageIndex].modesByShip[ship.id] =
                (ShipModeStorage[lastIndex()].modesByShip[ship.id]?.toMutableMap() ?: mutableMapOf())
//            Settings.tagStorage[AGCGUI.storageIndex].modesByShip[shipId] =
//                (Settings.tagStorage[lastIndex()].modesByShip[shipId]?.toMutableMap() ?: mutableMapOf())
            for(i in 0 until (ship.variant?.weaponGroups?.size ?: 0)) {
                val tags = loadPersistentTags(ship.id, ship, i, AGCGUI.storageIndex)
                persistTags(ship.id, ship, i, lastIndex(), tags)
            }
        }
    }

    override fun getTooltip(): String {
        return "Copy loadout #${lastIndex() + 1} into current loadout." +
                "\n$fleetBoilerplateText"
    }

    override fun getName(): String = "Copy previous loadout" + nameSuffix(allLoadouts = false)
}