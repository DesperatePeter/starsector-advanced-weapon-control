package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues.defaultFireModeString
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues.fireModeAsString
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues.fireModeDetailedDescriptions
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color


class ModeButton(ship: FleetMemberAPI, group : Int, mode : FireMode, button: ButtonAPI) : ButtonBase<FireMode>(ship, group, mode, button) {

    companion object{
        private var storage = FireModeStorage[AGCGUI.storageIndex]

        public fun createModeButtonGroup(ship: FleetMemberAPI, group: Int, tooltip: TooltipMakerAPI) : List<ModeButton>{
            storage = FireModeStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<ModeButton>()
            var isSomethingChecked = false
            Settings.cycleOrder().forEach {
                Misc.getBasePlayerColor()
                toReturn.add(ModeButton(ship, group, it, tooltip.addAreaCheckbox(fireModeAsString[it], it,
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 160f, 18f, 3f)))
                tooltip.addTooltipToPrevious(AGCGUI.makeTooltip(fireModeDetailedDescriptions[it] ?: ""), TooltipMakerAPI.TooltipLocation.BELOW)
                if(FMValues.FIRE_MODE_TRANSLATIONS[storage.modesByShip[ship.id]?.get(group)] == it){
                    toReturn.last().check()
                    isSomethingChecked = true
                }
                if (shouldModeBeDisabled(group, ship, it)){
                    toReturn.last().disable()
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
        storage.modesByShip[ship.id]?.let { it[group] = FMValues.fireModeAsString[associatedValue] ?: defaultFireModeString }
    }
}