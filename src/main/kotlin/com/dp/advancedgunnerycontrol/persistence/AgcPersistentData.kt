package com.dp.advancedgunnerycontrol.persistence

import com.fs.starfarer.api.Global

private object AgcPersistentDataLog

private val noSectorPersistentDataWarnings = mutableSetOf<String>()

fun agcPersistentDataOrNull(context: String): MutableMap<String, Any>? {
    val sector = Global.getSector()
    if (sector != null) return sector.persistentData
    if (noSectorPersistentDataWarnings.add(context)) {
        Global.getLogger(AgcPersistentDataLog::class.java)
            .warn("[AGC_PERSISTENCE] No sector available for $context; using non-persistent fallback.")
    }
    return null
}
