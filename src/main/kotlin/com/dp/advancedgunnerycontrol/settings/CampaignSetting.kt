package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global
import org.json.JSONObject
import kotlin.reflect.KProperty

class CampaignSettingDelegate<T>(private val key: String, private val defaultValue: T, private val getFromLunaSettingsIfPossible: Boolean = true)  {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T{
        val luna = if(getFromLunaSettingsIfPossible) LunaSettingHandler(key, defaultValue)() else null
        return luna ?: (Global.getSector().persistentData["$" + Values.THIS_MOD_NAME + key] as? T?) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T){
        Global.getSector().persistentData[key] = value
    }
}