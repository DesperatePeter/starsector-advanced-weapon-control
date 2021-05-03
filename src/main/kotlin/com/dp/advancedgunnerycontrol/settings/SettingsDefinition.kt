package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.FireModeStorage
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global
import data.scripts.util.MagicSettings
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

open class SettingsDefinition {
    var settings: JSONObject? = null

    private val settinglist = mutableListOf<Setting<*>>()

    protected fun <T> addSetting(key: String, defaultValue :T) : Setting<T>{
        Setting<T>(key, defaultValue).let {
            settinglist.add(it)
            return it
        }
    }

    fun loadSettings() {
        readSettings()
        applySettings()
    }

    protected open fun readSettings() {
        try {
            Global.getSettings().loadJSON(Values.SETTINGS_FILE_NAME).also { settings = it }
        } catch (e: IOException) {
            Global.getLogger(this.javaClass).warn(
                "Unable to load settings file settings.editme! Falling back to default settings", e
            )
        } catch (e: JSONException) {
            Global.getLogger(this.javaClass).warn(
                "Invalid settings file, please double-check! Falling back to default settings", e
            )
        }
    }

    protected open fun applySettings() {
        settinglist.forEach { setting ->
            settings?.let { setting.load(it) }
        }
    }

    open fun printSettings() {
        var toPrint = "\nAdvanced Gunnery Control Settings: \n"
        settinglist.forEach {
            toPrint += "${it.asString()} \n"
        }
        Global.getLogger(this.javaClass).info(toPrint)
    }

}