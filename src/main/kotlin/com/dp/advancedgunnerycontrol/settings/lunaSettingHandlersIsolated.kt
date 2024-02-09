package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener

// isolated in separate file to make sure that no import error is thrown when LunaLib is not present

fun<T> loadLunaSetting(key: String, defaultValue: T): T?{
    return when (defaultValue) {
        is Float -> (LunaSettings.getFloat(Values.THIS_MOD_ID, key) as? T)
        is Boolean -> (LunaSettings.getBoolean(Values.THIS_MOD_ID, key) as? T)
        is Int -> (LunaSettings.getInt(Values.THIS_MOD_ID, key) as? T)
        is String -> (LunaSettings.getString(Values.THIS_MOD_ID, key) as? T)
        else -> null
    }
}

fun addLunaSettingListener(callback: () -> Unit){
    LunaSettings.addSettingsListener(object : LunaSettingsListener{
        override fun settingsChanged(modID: String) {
            if(modID == Values.THIS_MOD_ID) callback()
        }

    })
}