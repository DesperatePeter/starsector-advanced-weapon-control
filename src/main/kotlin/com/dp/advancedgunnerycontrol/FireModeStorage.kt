package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.Global

typealias ModeStorage = MutableMap<String, MutableMap<Int, WeaponModeSelector>>
class FireModeStorage (){
    private val persistentDataKey = "$" + Values.THIS_MOD_NAME + "weaponModes"

    private fun getMap(wasFallback : Boolean = false) : ModeStorage{
        return (Global.getSector().persistentData[persistentDataKey] as? ModeStorage) ?: kotlin.run{
            Global.getSector().persistentData[persistentDataKey] = mutableMapOf<String, MutableMap<Int, WeaponModeSelector>>()
            if (wasFallback) return mutableMapOf()
            return getMap(true)
        }
    }

    var modesByShip : ModeStorage
        get() {
            return getMap()
        }
        private set(value) {
            Global.getSector().persistentData[persistentDataKey] = value
        }

    fun purge(){
        modesByShip = mutableMapOf()
    }
}