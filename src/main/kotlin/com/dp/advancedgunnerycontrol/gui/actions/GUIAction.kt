package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import org.lwjgl.input.Keyboard

abstract class GUIAction(protected var attributes: GUIAttributes) {
    abstract fun execute()

    abstract fun getTooltip() : String

    abstract fun getName() : String

    open fun getShortcut() : Int? = null

    protected val wholeFleetKey = "[Shift]"
    protected val allLoadoutsKey = "[Ctrl]"
    protected val fleetBoilerplateText = "Hold $wholeFleetKey to affect entire fleet."
    protected val loadoutBoilerplateText = "Hold $allLoadoutsKey to affect all loadouts."
    protected val modifiersBoilerplateText = "Affects current ship and current loadout." +
            "\n$loadoutBoilerplateText" + "\n$fleetBoilerplateText"

    protected fun affectedShips() : List<FleetMemberAPI>{
        return if (isWholeFleetKeyHeld()){
            Global.getSector().playerFleet.membersWithFightersCopy.filterNot { m -> m.isFighterWing }.filterNotNull()
        } else{
            attributes.ship?.let { listOf(it) } ?: emptyList()
        }
    }

    protected fun affectedLoadouts() : List<Int>{
        return if(isAllLoadoutsKeyHeld()){
            (0 until Settings.maxLoadouts()).toList()
        }else{
            listOf(AGCGUI.storageIndex)
        }
    }

    protected fun isAllLoadoutsKeyHeld() : Boolean{
        return Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
    }
    protected fun isWholeFleetKeyHeld() : Boolean{
        return Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
    }
    protected fun lastIndex() : Int{
        if (AGCGUI.storageIndex == 0) return Settings.maxLoadouts() -1
        return AGCGUI.storageIndex -1
    }
}