package com.dp.advancedgunnerycontrol.settings

import com.dp.advancedgunnerycontrol.FireModeStorage
import com.dp.advancedgunnerycontrol.typesandvalues.FMValues
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.Global
import data.scripts.util.MagicSettings
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.getFloat
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

object Settings {
    var settings: JSONObject? = null
    // TODO: list setting?
    val cycleOrderInternal = ListSetting("cycleOrder", listOf("PD", "Fighters", "Missiles", "NoFighters"))
    var cycleOrder = listOf(FireMode.DEFAULT)
    val enableCustomAI = BoolSetting("enableCustomAI", true)
    val customAIRecursionLevel = Setting<Int>("customAIRecursionLevel", 1)
    val forceCustomAI = BoolSetting("forceCustomAI", false)
    val customAITriggerHappiness = Setting<Float>("customAITriggerHappiness", 1.2f)
    val customAIPerfectTargetLeading = BoolSetting("customAIAlwaysUsesBestTargetLeading", false)
    val customAIFriendlyFireCaution = Setting<Float>("customAIFriendlyFireCaution", 1.0f)
    val customAIFriendlyFireComplexity = Setting<Int>("customAIFriendlyFireAlgorithmComplexity", 1)
    val uiDisplayFrames = Setting<Int>("messageDisplayDuration", 150)
    val uiPositionX = Setting<Int>("messagePositionX", 900)
    val uiPositionY = Setting<Int>("messagePositionY", 150)
    val uiForceFullInfo = BoolSetting("alwaysShowFullInfo", false)
    val infoHotkey = CharSetting("saveLoadInfoHotkey", 'j')
    val resetHotkey= CharSetting("resetHotkey", '/')
    val enablePersistentModes = BoolSetting("enablePersistentFireModes", true)
    private val settinglist = listOf(
        cycleOrderInternal, enableCustomAI, customAIRecursionLevel, forceCustomAI, customAITriggerHappiness,
        customAIPerfectTargetLeading, customAIFriendlyFireCaution, customAIFriendlyFireComplexity, uiDisplayFrames,
        uiPositionX, uiPositionY, uiForceFullInfo, infoHotkey, resetHotkey, enablePersistentModes)
    var weaponBlacklist = listOf<String>()
        private set

    val shipModeStorage = FireModeStorage()

    fun buildCycleOrder(additionalItems: List<String>): List<FireMode> {
        return (listOf(FireMode.DEFAULT) + additionalItems.mapNotNull {
            FMValues.FIRE_MODE_TRANSLATIONS[it]
        })
    }


    fun loadSettings() {
        readSettings()
        applySettings()
    }

    private fun readSettings() {
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
        weaponBlacklist = MagicSettings.getList(Values.THIS_MOD_NAME, Values.WEAPON_BLACKLIST_KEY)
    }

    private fun applySettings() {
        settinglist.forEach { setting ->
            settings?.let { setting.load(it) }
        }
        forceCustomAI.set(forceCustomAI() && enableCustomAI())
        customAIFriendlyFireComplexity.set ( max(0, min(2, customAIFriendlyFireComplexity())))
        infoHotkey.set(infoHotkey().toLowerCase())
        resetHotkey.set(resetHotkey().toLowerCase())
        cycleOrder = cycleOrder + cycleOrderInternal().mapNotNull { FMValues.FIRE_MODE_TRANSLATIONS[it] }
    }

}