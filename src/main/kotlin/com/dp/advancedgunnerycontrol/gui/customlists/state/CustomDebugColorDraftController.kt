package com.dp.advancedgunnerycontrol.gui.customlists.state

import java.awt.Color

internal class CustomDebugColorDraftController(
    private val draftStore: CustomListDraftStore,
) {
    fun debugColorIndex(maxIndex: Int): Int {
        return (draftStore.value(CustomListDraftKeys.DebugColor.INDEX)?.toIntOrNull() ?: 0)
            .coerceIn(0, maxIndex.coerceAtLeast(0))
    }

    fun setDebugColorIndex(index: Int) {
        draftStore.writeValue(CustomListDraftKeys.DebugColor.INDEX, index.toString())
    }

    fun debugColorDraft(fallback: Color): Color {
        return Color(
            (draftStore.value(CustomListDraftKeys.DebugColor.RED)?.toIntOrNull() ?: fallback.red).coerceIn(0, 255),
            (draftStore.value(CustomListDraftKeys.DebugColor.GREEN)?.toIntOrNull() ?: fallback.green).coerceIn(0, 255),
            (draftStore.value(CustomListDraftKeys.DebugColor.BLUE)?.toIntOrNull() ?: fallback.blue).coerceIn(0, 255),
            fallback.alpha
        )
    }

    fun hasDebugColorDraftRgb(): Boolean {
        val values = draftStore.values()
        return values.containsKey(CustomListDraftKeys.DebugColor.RED) &&
            values.containsKey(CustomListDraftKeys.DebugColor.GREEN) &&
            values.containsKey(CustomListDraftKeys.DebugColor.BLUE)
    }

    fun setDebugColorDraft(color: Color) {
        val values = draftStore.values()
        values[CustomListDraftKeys.DebugColor.RED] = color.red.coerceIn(0, 255).toString()
        values[CustomListDraftKeys.DebugColor.GREEN] = color.green.coerceIn(0, 255).toString()
        values[CustomListDraftKeys.DebugColor.BLUE] = color.blue.coerceIn(0, 255).toString()
        draftStore.writeValues(values)
    }

    fun debugColorPersistent(): Boolean {
        return draftStore.value(CustomListDraftKeys.DebugColor.PERSISTENCE)?.toBooleanStrictOrNull() ?: false
    }

    fun setDebugColorPersistent(persistent: Boolean) {
        draftStore.writeValue(CustomListDraftKeys.DebugColor.PERSISTENCE, persistent.toString())
    }
}
