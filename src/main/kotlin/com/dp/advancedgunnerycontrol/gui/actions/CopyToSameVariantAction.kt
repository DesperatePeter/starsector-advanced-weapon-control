package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
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
                "\n$loadoutBoilerplateText"
    }

    override fun getName(): String = "Copy to other ships of same variant"

    private fun applyModesToSameVariantShips(ship: FleetMemberAPI, storageIndex: Int){
        Global.getSector().playerFleet.membersWithFightersCopy.filter{!it.isFighterWing && it != ship}.filterNotNull().forEach {
            if (it.variant.hullVariantId + it.variant.displayName == ship.variant.hullVariantId + ship.variant.displayName){
//                FireModeStorage[storageIndex].modesByShip[ship.id]?.let { v ->
//                    FireModeStorage[storageIndex].modesByShip[it.id] = v.toMutableMap()
//                }
                ShipModeStorage[storageIndex].modesByShip[ship.id]?.let { v ->
                    ShipModeStorage[storageIndex].modesByShip[it.id] = v.toMutableMap()
                }
//                SuffixStorage[storageIndex].modesByShip[ship.id]?.let { v ->
//                    SuffixStorage[storageIndex].modesByShip[it.id] = v.toMutableMap()
//                }
                Settings.tagStorage[storageIndex].modesByShip[ship.id]?.let { v->
                    Settings.tagStorage[storageIndex].modesByShip[it.id] = v.toMutableMap()
                }
            }
        }
    }
}