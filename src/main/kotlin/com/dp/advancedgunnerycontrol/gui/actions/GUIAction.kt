package com.dp.advancedgunnerycontrol.gui.actions


import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import org.lwjgl.input.Keyboard
import kotlin.reflect.KProperty

abstract class GUIAction(protected var attributes: GUIAttributes) {
    abstract fun execute()

    abstract fun getTooltip(): String

    abstract fun getName(): String

    open fun getStableLayoutName(): String = getName()

    open fun supportsRightClick(): Boolean = false

    open fun executeRightClick(): Boolean = false

    open fun getConfirmationDescription(): String = "Confirming will execute ${getName()}."

    open fun getConfirmationTitle(): String = "Confirm ${getName()}"

    open fun getConfirmationTone(): CampaignConfirmationTone = CampaignConfirmationTone.CAUTION

    open fun requiresConfirmation(): Boolean = false

    open fun getShortcut(): Int? = null

    open fun getDisplayShortcut(): Int? = getShortcut()

    protected val wholeFleetKey = "[Shift]"
    protected val allLoadoutsKey = "[Ctrl]"
    protected val fleetBoilerplateText = "Hold $wholeFleetKey to affect entire fleet."
    protected val loadoutBoilerplateText = "Hold $allLoadoutsKey to affect all loadouts."
    protected val modifiersBoilerplateText = "Affects current ship and current loadout." +
            "\n$loadoutBoilerplateText" + "\n$fleetBoilerplateText"

    companion object{
        fun isAllLoadoutsKeyHeld(): Boolean {
            return Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
        }

        fun isWholeFleetKeyHeld(): Boolean {
            return Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
        }

        fun modifierKeys(): Pair<Boolean, Boolean>{
            return Pair(isAllLoadoutsKeyHeld(), isWholeFleetKeyHeld())
        }
    }

    protected fun affectedShips(): List<FleetMemberAPI> {
        return affectedShips(isWholeFleetKeyHeld())
    }

    protected fun affectedShips(wholeFleet: Boolean): List<FleetMemberAPI> {
        return if (wholeFleet) {
            Global.getSector()?.playerFleet?.membersWithFightersCopy
                ?.filterNot { m -> m.isFighterWing }
                .orEmpty()
        } else {
            attributes.ship?.let { listOf(it) } ?: emptyList()
        }
    }

    protected fun affectedLoadouts(): List<Int> {
        return if (isAllLoadoutsKeyHeld()) {
            (0 until Settings.maxLoadouts()).toList()
        } else {
            listOf(AGCGUI.storageIndex)
        }
    }

    protected fun lastIndex(): Int {
        if (AGCGUI.storageIndex == 0) return Settings.maxLoadouts() - 1
        return AGCGUI.storageIndex - 1
    }

    protected fun nameSuffix(allLoadouts: Boolean = true, wholeFleet: Boolean = true) : String {
        var suffix = ""
        if(isAllLoadoutsKeyHeld() && allLoadouts) suffix += " for all loadouts"
        if(isWholeFleetKeyHeld() && wholeFleet) suffix += " for entire fleet"
        return suffix
    }
}
