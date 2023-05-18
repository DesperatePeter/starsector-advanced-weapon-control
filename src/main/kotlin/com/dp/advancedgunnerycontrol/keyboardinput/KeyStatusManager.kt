package com.dp.advancedgunnerycontrol.keyboardinput

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.input.InputEventAPI

class KeyStatusManager {
    var mkeyStatus = KeyStatus()

    /**
     * @return true if event relevant
     */
    private fun parseInputEvent(event: InputEventAPI): Boolean {
        if (event.isConsumed || !event.isKeyDownEvent) return false

        when (event.eventChar.lowercaseChar()) {
            Settings.infoHotkey() -> mkeyStatus.mcontrolEvent = ControlEventType.INFO
            // FIXME: Settings
            'k' -> mkeyStatus.mcontrolEvent = ControlEventType.MERGE
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