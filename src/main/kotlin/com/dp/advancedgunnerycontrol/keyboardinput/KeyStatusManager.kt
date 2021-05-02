package com.dp.advancedgunnerycontrol.keyboardinput

import com.dp.advancedgunnerycontrol.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.ControlEventType
import com.fs.starfarer.api.input.InputEventAPI

class KeyStatusManager {
    var mkeyStatus = KeyStatus()
    private val weaponGroupKeys = '1'..'7'

    /**
     * @return true if event relevant
     */
    private fun parseInputEvent(event: InputEventAPI): Boolean {
        if (event.isConsumed || !event.isKeyDownEvent) return false

        if (event.eventChar == Settings.infoHotkey) {
            mkeyStatus.mcontrolEvent = ControlEventType.INFO
            return true
        }

        if (event.eventChar == Settings.resetHotkey){
            mkeyStatus.mcontrolEvent = ControlEventType.RESET
            return true
        }

        if (event.eventChar !in weaponGroupKeys) return false
        // Note: char.toInt gets the ascii value rather than the contained number
        mkeyStatus.mpressedWeaponGroup = event.eventChar.toString().toInt()

        mkeyStatus.mcontrolEvent = ControlEventType.CYCLE
        if (event.isAltDown) {
            mkeyStatus.mcontrolEvent = ControlEventType.COMBINE
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
            wasRelevant = wasRelevant || parseInputEvent(it)
        }
        return wasRelevant
    }
}