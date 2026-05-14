package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.persistence.agcPersistentDataOrNull
import com.fs.starfarer.api.Global
import org.json.JSONObject
import kotlin.reflect.KProperty

class CampaignSettingDelegate<T>(private val key: String, private val defaultValue: T, private val getFromLunaSettingsIfPossible: Boolean = true)  {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T{
        val luna = if(getFromLunaSettingsIfPossible) LunaSettingHandler(key, defaultValue)() else null
        if (luna != null) return luna
        val persistentKey = "$" + Values.THIS_MOD_NAME + key
        val persistentData = agcPersistentDataOrNull("campaign setting '$key' read") ?: return defaultValue
        val stored = persistentData[persistentKey]
        val parsed = readStoredValue(stored)
        if (parsed != null) return parsed
        if (stored != null) {
            Global.getLogger(this.javaClass).warn("Ignoring invalid campaign setting '$key' with value '$stored'.")
        }
        return defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T){
        agcPersistentDataOrNull("campaign setting '$key' write")
            ?.set("$" + Values.THIS_MOD_NAME + key, value as Any)
    }

    @Suppress("UNCHECKED_CAST")
    private fun readStoredValue(stored: Any?): T? {
        if (stored == null) return null
        return when (defaultValue) {
            is Boolean -> stored as? T
            is Int -> stored as? T
            is Float -> stored as? T
            is String -> stored as? T
            is List<*> -> readStringList(stored) as? T
            is Map<*, *> -> readStringListMap(stored) as? T
            else -> stored as? T
        }
    }

    private fun readStringList(stored: Any?): List<String>? {
        val list = stored as? List<*> ?: return null
        if (list.any { it !is String }) return null
        return list.filterIsInstance<String>()
    }

    private fun readStringListMap(stored: Any?): Map<String, List<String>>? {
        val map = stored as? Map<*, *> ?: return null
        val parsed = mutableMapOf<String, List<String>>()
        map.forEach { (rawKey, rawValue) ->
            val stringKey = rawKey as? String ?: return null
            val stringList = readStringList(rawValue) ?: return null
            parsed[stringKey] = stringList
        }
        return parsed
    }
}
