package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global

open class StorageBase<T> (private val persistentDataKey: String){

    private fun getMap(wasFallback: Boolean = false): MutableMap<String, MutableMap<Int, T>> {
        return (Global.getSector().persistentData[persistentDataKey] as? MutableMap<String, MutableMap<Int, T>>) ?: kotlin.run {
            Global.getSector().persistentData[persistentDataKey] =
                mutableMapOf<String, MutableMap<Int, T>>()
            if (wasFallback) return mutableMapOf()
            return getMap(true)
        }
    }

    var modesByShip: MutableMap<String, MutableMap<Int, T>>
        get() {
            if(!Settings.enablePersistentModes()){
                return mutableMapOf()
            }
            return getMap()
        }
        private set(value) {
            Global.getSector().persistentData[persistentDataKey] = value
        }

    fun purge() {
        Global.getSector().persistentData.remove(persistentDataKey)
    }
}