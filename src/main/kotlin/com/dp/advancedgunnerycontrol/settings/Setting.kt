package com.dp.advancedgunnerycontrol.settings

import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.getFloat

// Settings are loaded through the runtime default value's type because the JSON
// config is intentionally simple and only supports AGC's primitive/list setting
// shapes. Unknown types fall back to their existing value instead of crashing
// during settings reload.
open class Setting<T>(private val key: String, private val defaultValue: T, private val getFromLunaSettingsIfPossible: Boolean = true) {
    var value: T = defaultValue
    val lunaSettingHandler = LunaSettingHandler(key, defaultValue)

    fun load(json: JSONObject) {
        try {
            if(getFromLunaSettingsIfPossible){
                lunaSettingHandler()?.let {
                    value = it
                    return
                }
            }
            value = (loadImpl(json) ?: kotlin.run {
                logError()
                value
            })
        } catch (e: JSONException) {
            logError()
        }
    }

    open fun loadImpl(json: JSONObject): T? {
        return when (value) {
            is Char -> ((json.get(key) as? String)?.get(0)?.lowercaseChar() as? T)
            is Boolean -> (json.get(key) == true) as? T
            is Int -> json.getInt(key) as? T
            is Float -> json.getFloat(key) as? T
            is String -> json.getString(key) as? T
            is List<*> -> {
                val list = mutableListOf<String>()
                json.apply {
                    val array = getJSONArray(key)
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i))
                    }
                }
                list as? T
            }

            else -> null
        }
    }

    operator fun invoke(): T {
        return value
    }

    fun asString(): String {
        return "$key: $value"
    }

    fun set(value: T) {
        this.value = value
    }

    fun logError() {
        Global.getLogger(this.javaClass).warn(
            """
                Error when loading $key, using default default value $value instead.
            """.trimIndent()
        )
    }

    fun resetToDefault(){
        value = defaultValue
    }
}
