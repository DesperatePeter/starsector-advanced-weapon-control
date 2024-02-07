package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import lunalib.lunaSettings.LunaSettings

class LunaSettingHandler<T>(private val key: String, private val defaultValue: T) {
    operator fun invoke(): T{
        return when (defaultValue) {
            is Float -> (LunaSettings.getFloat(Values.THIS_MOD_ID, key) as? T) ?: defaultValue
            is Boolean -> (LunaSettings.getBoolean(Values.THIS_MOD_ID, key) as? T) ?: defaultValue
            is Int -> (LunaSettings.getInt(Values.THIS_MOD_ID, key) as? T) ?: defaultValue
            is String -> (LunaSettings.getString(Values.THIS_MOD_ID, key) as? T) ?: defaultValue
            else -> defaultValue
        }
    }
}