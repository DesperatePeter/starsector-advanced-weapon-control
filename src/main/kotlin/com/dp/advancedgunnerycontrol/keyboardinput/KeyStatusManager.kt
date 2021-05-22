package com.dp.advancedgunnerycontrol.keyboardinput

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.input.InputEventAPI

class KeyStatusManager {
    var mkeyStatus = KeyStatus()
    private val weaponGroupKeys = '1'..'7'

    /**
     * @return true if event relevant
     */
    private fun parseInputEvent(event: InputEventAPI): Boolean {
        if (event.isConsumed || !event.isKeyDownEvent) return false

        when(event.eventChar.toLowerCase()){
            Settings.infoHotkey() -> mkeyStatus.mcontrolEvent = ControlEventType.INFO
            Settings.resetHotkey() -> mkeyStatus.mcontrolEvent = ControlEventType.RESET
            Settings.loadHotkey() -> mkeyStatus.mcontrolEvent = ControlEventType.LOAD
            Settings.suffixHotkey() -> mkeyStatus.mcontrolEvent = ControlEventType.SUFFIX
            Settings.cycleLoadout() -> mkeyStatus.mcontrolEvent = ControlEventType.CYCLE_LOADOUT

            in weaponGroupKeys -> {
                mkeyStatus.mpressedWeaponGroup = event.eventChar.toString().toInt()
                mkeyStatus.lastPressedWeaponGroup = mkeyStatus.mpressedWeaponGroup
                mkeyStatus.mcontrolEvent = ControlEventType.CYCLE
                if (event.isAltDown) {
                    mkeyStatus.mcontrolEvent = ControlEventType.COMBINE
                }
            }
            else -> return false
        }
        event.consume()
        return true
    }

    /**
     * @return true if a relevant event occurred
     */
    fun parseInputEvents(events: MutableList<InputEventAPI>?): Boolean {
        mkeyStatus.reset()
        var wasRelevant = false
        events?.iterator()?.forEach {
            wasRelevant = (wasRelevant || parseInputEvent(it))
        }
        return wasRelevant
    }
}