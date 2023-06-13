package com.dp.advancedgunnerycontrol.gui


import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.ShipModeStorage
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc

class ShipModeButton(var ship: FleetMemberAPI,  mode: ShipModes, button: ButtonAPI) :
    ButtonBase<ShipModes>(mode, button, false) {

    companion object {

        fun createModeButtonGroup(
            ship: FleetMemberAPI,
            panel: CustomPanelAPI,
            position: UIComponentAPI
        ): List<ShipModeButton> {
            val toReturn = mutableListOf<ShipModeButton>()
            val elementList = mutableListOf<TooltipMakerAPI>()
            Settings.getCurrentShipModes().forEach {
                val tooltip = panel.createUIElement(130f, 30f, false)
                toReturn.add(
                    ShipModeButton(
                        ship, it, tooltip.addAreaCheckbox(
                            shipModeToString[it],
                            it,
                            Misc.getBasePlayerColor(),
                            Misc.getDarkPlayerColor(),
                            Misc.getBrightPlayerColor(),
                            130f,
                            18f,
                            3f
                        )
                    )
                )
                tooltip.addTooltipToPrevious(
                    AGCGUI.makeTooltip(detailedShipModeDescriptions[it] ?: ""),
                    TooltipMakerAPI.TooltipLocation.BELOW
                )
                if (elementList.isEmpty()) {
                    panel.addUIElement(tooltip).belowLeft(position, 5f)
                } else {
                    panel.addUIElement(tooltip).rightOfTop(elementList.last(), 12f)
                }
                elementList.add(tooltip)
            }

            toReturn.forEach {
                it.sameGroupButtons = toReturn
                it.updateIfCheckedBasedOnData()
            }
            return toReturn
        }
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked) {
            check()
        } else if (active && !button.isChecked) {
            removePersistentShipMode(ship.id, AGCGUI.storageIndex, shipModeToString[associatedValue] ?: defaultShipMode)
            uncheck()
        }
        button.isChecked = active
        sameGroupButtons.forEach { (it as? ShipModeButton)?.updateIfCheckedBasedOnData() }
    }

    private fun updateIfCheckedBasedOnData(){
        if (loadPersistedShipModes(ship.id, AGCGUI.storageIndex).contains(shipModeToString[associatedValue] ?: defaultShipMode)){
            check()
        }else{
            uncheck()
        }
    }

    override fun onActivate() {
        val id = ship.id
        val index = AGCGUI.storageIndex
        if (associatedValue == ShipModes.DEFAULT) {
            persistShipModes(id, index, emptyList())
        }
        if (associatedValue != ShipModes.DEFAULT) {
            removePersistentShipMode(id, index, defaultShipMode)
        }
        addPersistentShipMode(id, index, shipModeToString[associatedValue] ?: defaultShipMode)
    }
}