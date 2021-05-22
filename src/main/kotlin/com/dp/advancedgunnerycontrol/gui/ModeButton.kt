package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues.defaultFireModeString
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color


class ModeButton(ship: FleetMemberAPI, group : Int, mode : FireMode, button: ButtonAPI) : ButtonBase<FireMode>(ship, group, mode, button) {

    companion object{
        private var storage = FireModeStorage[AGCGUI.storageIndex]

        public fun createModeButtonGroup(ship: FleetMemberAPI, group: Int, tooltip: TooltipMakerAPI) : List<ModeButton>{
            storage = FireModeStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<ModeButton>()
            var isSomethingChecked = false
            Settings.cycleOrder().forEach {
                toReturn.add(ModeButton(ship, group, it, tooltip.addAreaCheckbox(it.toString(), it,
                    Color.BLUE, Color.BLUE, Color.WHITE, 160f, 24f, 3f)))
                if(FMValues.FIRE_MODE_TRANSLATIONS[storage.modesByShip[ship.id]?.get(group)] == it){
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
        storage.modesByShip[ship.id]?.let { it[group] = FMValues.fireModeAsString[associatedValue] ?: defaultFireModeString }
    }
}