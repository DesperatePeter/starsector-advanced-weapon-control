package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global

typealias StorageBaseIntKey<ValueType> = StorageBase<Int, ValueType>
open class StorageBase<KeyType, ValueType>(val persistentDataKey: String) {

    companion object {
        fun <KeyType, ValueType> assembleStorageArray(baseKey: String, size: Int = Settings.maxLoadouts()): List<StorageBase<KeyType, ValueType>> {
            val toReturn = mutableListOf(StorageBase<KeyType, ValueType>(baseKey))
            for (i in 1 until size) {
                toReturn.add(StorageBase(baseKey + i.toString()))
            }
            return toReturn.toList()
        }
    }

    private fun getMap(wasFallback: Boolean = false): MutableMap<String, MutableMap<KeyType, ValueType>> {
        return (Global.getSector().persistentData[persistentDataKey] as? MutableMap<String, MutableMap<KeyType, ValueType>>?)
            ?: kotlin.run {
                Global.getSector().persistentData.remove(persistentDataKey)
                Global.getSector().persistentData[persistentDataKey] =
                    mutableMapOf<String, MutableMap<KeyType, ValueType>>()
                if (wasFallback) return mutableMapOf()
                return getMap(true)
            }
    }

    var modesByShip: MutableMap<String, MutableMap<KeyType, ValueType>>
        get() {
            if (!Settings.enablePersistentModes()) {
                return mutableMapOf()
            }
            return getMap()
        }
        set(value) {
            Global.getSector().persistentData[persistentDataKey] = value
        }

    inline fun <reified T> purgeIfNecessary() {
        val map = Global.getSector().persistentData[persistentDataKey] as? MutableMap<*, *>?
        if (map == null) {
            purge(); return
        }
        if (map.isEmpty()) {
            purge(); return
        }
        val subMap = (map.values.firstOrNull() as? MutableMap<*, *>)
        if (subMap == null) {
            purge(); return
        }
        if (subMap.values.firstOrNull() !is T) {
            purge(); return
        }
        if (!Settings.enablePersistentModes()) {
            purge(); return
        }
    }

    fun purge() {
        Global.getSector().persistentData.remove(persistentDataKey)
    }
}