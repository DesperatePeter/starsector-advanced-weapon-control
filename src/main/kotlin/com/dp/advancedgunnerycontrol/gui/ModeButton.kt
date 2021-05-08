package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.utils.FireModeStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color


class ModeButton(private var ship: FleetMemberAPI, private var group : Int, private var mode : FireMode, var button: ButtonAPI) {
    private var active = false
    private var sameGroupButtons : List<ModeButton> = emptyList()
        set(value) {field = value.filter { it.mode != mode }}

    companion object{
        private val storage = FireModeStorage()

        public fun createModeButtonGroup(ship: FleetMemberAPI, group: Int, tooltip: TooltipMakerAPI) : List<ModeButton>{
            val toReturn = mutableListOf<ModeButton>()

            Settings.cycleOrder().forEach {
                toReturn.add(ModeButton(ship, group, it, tooltip.addAreaCheckbox(it.toString(), it,
                    Color.BLUE, Color.BLUE, Color.WHITE, 120f, 24f, 3f)))
                if(storage.modesByShip[ship.id]?.get(group)?.currentMode == it) toReturn.last().check()
            }
            toReturn.forEach {
                it.sameGroupButtons = toReturn.filter { it1 -> it1 != it }
            }
            return toReturn
        }
    }

    public fun executeCallbackIfChecked(){
        if (!active && button.isChecked){
            check()
        }
        button.isChecked = active
    }

    private fun check(){
        callback()
        active = true
        button.isChecked = true
    }

    private fun uncheck(){
        active = false
        button.isChecked = false
    }

    private fun callback(){
        sameGroupButtons.forEach { it.uncheck() }
        if(storage.modesByShip[ship.id] == null){
            storage.modesByShip[ship.id] = mutableMapOf()
        }
        storage.modesByShip[ship.id]?.let { it[group] = WeaponModeSelector(mode) }
    }
}