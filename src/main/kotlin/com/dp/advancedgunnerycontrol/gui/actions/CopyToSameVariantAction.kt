package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI

class CopyToSameVariantAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.ship?.let { ship ->
            affectedLoadouts().forEach { index ->
                applyModesToSameVariantShips(ship, index)
            }
        }
    }

    override fun getTooltip(): String {
        return "This will override the modes for all other ships with the same " +
                "hull and variant name with the modes set for the current ship.\n" +
                "This is useful if you used auto-fit to set up multiple identical ships." +
                "\n$loadoutBoilerplateText" +
                "\nHold $wholeFleetKey to instead copy to all ships of the same hull type."
    }

    override fun getName(): String = "Copy to other ships of same variant" + nameSuffix(wholeFleet = false) + if(isWholeFleetKeyHeld()) " (same hull type)" else ""

    private fun applyModesToSameVariantShips(ship: FleetMemberAPI, storageIndex: Int) {
        Global.getSector().playerFleet.membersWithFightersCopy.filter { !it.isFighterWing && it != ship }
            .filterNotNull().forEach {
                val shouldCopy =
                    (it.variant.hullVariantId + it.variant.displayName == ship.variant.hullVariantId + ship.variant.displayName)
                            || (isWholeFleetKeyHeld() && (it.hullId == ship.hullId))
                if (shouldCopy) {
                    copyModesToShip(ship, it, storageIndex)
                }
            }
    }

    private fun copyModesToShip(from: FleetMemberAPI, to: FleetMemberAPI, storageIndex: Int) {
        ShipModeStorage[storageIndex].modesByShip[from.id]?.let { v ->
            ShipModeStorage[storageIndex].modesByShip[to.id] = v.toMutableMap()
        }
        Settings.tagStorage[storageIndex].modesByShip[from.id]?.let { v ->
            Settings.tagStorage[storageIndex].modesByShip[to.id] = v.toMutableMap()
        }
    }
}