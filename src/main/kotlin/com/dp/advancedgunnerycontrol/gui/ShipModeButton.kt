package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color

class ShipModeButton(ship: FleetMemberAPI, mode : ShipModes, button: ButtonAPI) : ButtonBase<ShipModes>(ship, 0, mode, button) {

    companion object{
        private var storage = ShipModeStorage[AGCGUI.storageIndex]

        public fun createModeButtonGroup(ship: FleetMemberAPI, tooltip: TooltipMakerAPI) : List<ShipModeButton>{
            storage = ShipModeStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<ShipModeButton>()
            var isSomethingChecked = false
            ShipModes.values().forEach {
                toReturn.add(ShipModeButton(ship, it, tooltip.addAreaCheckbox(
                    shipModeToString[it], it, Color.BLUE, Color.BLUE, Color.WHITE, 160f, 24f, 3f)))

                if(shipModeFromString[storage.modesByShip[ship.id]?.values?.firstOrNull()]== it) {
                    toReturn.last().check()
                    isSomethingChecked = true
                }
            }
            if(!isSomethingChecked) toReturn.firstOrNull()?.check()
            toReturn.forEach {
                it.sameGroupButtons = toReturn
            }
            return toReturn
        }
    }

    override fun onActivate() {
        if(storage.modesByShip[ship.id] == null){
            storage.modesByShip[ship.id] = mutableMapOf()
        }
        storage.modesByShip[ship.id]?.let { it[group] = shipModeToString[associatedValue] ?: defaultShipMode }
    }
}