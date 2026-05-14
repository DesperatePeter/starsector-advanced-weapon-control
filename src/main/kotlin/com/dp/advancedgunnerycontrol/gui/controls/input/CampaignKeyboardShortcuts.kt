package com.dp.advancedgunnerycontrol.gui.controls.input

import com.fs.starfarer.api.input.InputEventAPI

internal object CampaignKeyboardShortcuts {
    fun processCampaignKeyboardShortcutInput(
        events: MutableList<InputEventAPI>,
        closeKeys: Set<Int>,
        onClose: () -> Unit,
        onShortcut: (Int) -> Boolean,
    ): Boolean {
        events.forEach { event ->
            if (event.isConsumed || !event.isKeyDownEvent) return@forEach
            val key = event.eventValue
            if (key in closeKeys) {
                event.consume()
                onClose()
                return true
            }
            if (onShortcut(key)) {
                event.consume()
                return true
            }
        }
        return false
    }
}
