package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color

class ShipModeButton(ship: FleetMemberAPI, mode : ShipModes, button: ButtonAPI) : ButtonBase<ShipModes>(ship, 0, mode, button) {

    companion object{
        private var storage = ShipModeStorage[AGCGUI.storageIndex]

        public fun createModeButtonGroup(ship: FleetMemberAPI, panel: CustomPanelAPI) : List<ShipModeButton>{
            storage = ShipModeStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<ShipModeButton>()
            var isSomethingChecked = false
            val elementList = mutableListOf<TooltipMakerAPI>()
            ShipModes.values().forEach {
                val tooltip = panel.createUIElement(160f, 30f, false)
                toReturn.add(ShipModeButton(ship, it, tooltip.addAreaCheckbox(
                    shipModeToString[it], it, Color.BLUE, Color.BLUE, Color.WHITE, 160f, 24f, 3f)))
                tooltip.addTooltipToPrevious(AGCGUI.makeTooltip(detailedShipModeDescriptions[it] ?: ""), TooltipMakerAPI.TooltipLocation.BELOW)
                if(ShipModes.HELP == it) toReturn.lastOrNull()?.disable()
                if(shipModeFromString[storage.modesByShip[ship.id]?.values?.firstOrNull()]== it) {
                    toReturn.last().check()
                    isSomethingChecked = true
                }
                if(elementList.isEmpty()){
                    panel.addUIElement(tooltip).inTL(5f, 25f)
                }else{
                    panel.addUIElement(tooltip).rightOfTop(elementList.last(), 12f)
                }
                elementList.add(tooltip)
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