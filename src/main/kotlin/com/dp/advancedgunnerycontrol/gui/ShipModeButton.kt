package com.dp.advancedgunnerycontrol.gui


import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color
import java.util.function.Consumer

class ShipModeButton(ship: FleetMemberAPI, associatedIndex : Int, mode : ShipModes, button: ButtonAPI) : ButtonBase<ShipModes>(ship, associatedIndex, mode, button) {

    companion object{
        private var storage = ShipModeStorage[AGCGUI.storageIndex]

        fun createModeButtonGroup(ship: FleetMemberAPI, panel: CustomPanelAPI) : List<ShipModeButton>{
            storage = ShipModeStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<ShipModeButton>()
            var isSomethingChecked = false
            val elementList = mutableListOf<TooltipMakerAPI>()
            ShipModes.values().forEachIndexed { index, it ->
                val tooltip = panel.createUIElement(160f, 30f, false)
                toReturn.add(ShipModeButton(ship, index, it, tooltip.addAreaCheckbox(
                    shipModeToString[it], it, Color.BLUE, Color.BLUE, Color.WHITE, 160f, 18f, 3f)))
                tooltip.addTooltipToPrevious(AGCGUI.makeTooltip(detailedShipModeDescriptions[it] ?: ""), TooltipMakerAPI.TooltipLocation.BELOW)
                if(ShipModes.HELP == it) toReturn.lastOrNull()?.disable()
                if(storage.modesByShip[ship.id]?.values?.contains(shipModeToString[it]) == true) {
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
            var iter = toReturn.iterator()
            iter.next().sameGroupButtons = toReturn
            // note: forEachRemaining requires java 8
            while(iter.hasNext()){
                iter.next().sameGroupButtons = listOf(toReturn.first())
            }
            return toReturn
        }
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked){
            check()
        } else if(active && !button.isChecked){
            storage.modesByShip[ship.id]?.remove(group)
            uncheck()
        }
        button.isChecked = active
    }

    override fun onActivate() {
        if(storage.modesByShip[ship.id] == null || associatedValue == ShipModes.DEFAULT){
            storage.modesByShip[ship.id] = mutableMapOf()
        }
        if(associatedValue != ShipModes.DEFAULT){
            storage.modesByShip[ship.id]?.remove(0)
        }
        storage.modesByShip[ship.id]?.let { it[group] = shipModeToString[associatedValue] ?: defaultShipMode }
    }
}