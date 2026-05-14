package com.dp.advancedgunnerycontrol.gui.session

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

internal object ShipViewHotTagCache {
    private data class Key(
        val shipId: String,
        val maxLoadouts: Int,
    )

    private val loadedKeys = mutableSetOf<Key>()

    fun ensureLoaded(context: ShipEditorPersistenceContext) {
        val key = Key(
            shipId = context.shipId,
            maxLoadouts = Settings.maxLoadouts(),
        )
        if (!loadedKeys.add(key)) return
        Settings.hotAddTags(context.loadAllUsedTags())
    }

    fun invalidate() {
        loadedKeys.clear()
    }
}
