package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


public class WeaponControlBasePlugin : BaseModPlugin() {
    var settings: JSONObject? = null

    // TODO: Migrate to separate class?
    companion object Settings {
        var cycleOrder = buildCycleOrder(Values.DEFAULT_CYCLE_ORDER)
            private set
        var enableCustomAI = Values.DEFAULT_ENABLE_CUSTOM_AI
            private set
        var customAIRecursionLevel = Values.DEFAULT_AI_RECURSION_LEVEL
            private set
        var forceCustomAI = Values.DEFAULT_FORCE_CUSTOM_AI
            private set

        fun buildCycleOrder(additionalItems: List<String>): List<FireMode> {
            return (listOf(FireMode.DEFAULT) + additionalItems.mapNotNull {
                Values.FIRE_MODE_TRANSLATIONS[it]
            })
        }
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()
        // Note: In the future, reading a setting file might go here
        Global.getLogger(this.javaClass).info("Loaded AdvancedGunneryControl!")
        readSettings()
        applySettings()
        modifyFighterAndMissileModeDescriptionsToIncludeAIType()
        Global.getLogger(this.javaClass).info("Using cycle order:$cycleOrder")
        Global.getLogger(this.javaClass).info(
            "Using custom AI: $enableCustomAI, force: $forceCustomAI, recursion lvl :$customAIRecursionLevel"
        )
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
    }

    private fun applySettings() {
        // Enable custom AI
        try {
            settings?.apply { enableCustomAI = (get(Values.SETTINGS_ENABLE_CUSTOM_AI_KEY) == true) }
        } catch (e: JSONException) {
            Global.getLogger(this.javaClass).warn(
                """Key 'enableCustomAI' in ${Values.SETTINGS_FILE_NAME} invalid or not present.
                    | Entry should be: "enableCustomAI" : true/false """.trimMargin(), e
            )
        }

        // cycle order
        try {
            settings?.apply {
                val array = getJSONArray(Values.SETTINGS_CYCLE_ORDER_KEY)
                var modesToAdd = mutableListOf<String>()
                for (i in 0 until array.length()) { // forEach fails for some reason, so...manual for-loop...
                    modesToAdd.add(array.getString(i))
                }
                cycleOrder = buildCycleOrder(modesToAdd)
            }
        } catch (e: JSONException) {
            Global.getLogger(this.javaClass).warn(
                """Key "cycleOrder" in ${Values.SETTINGS_FILE_NAME} not present or content invalid.
                    | Entry should be: "cycleOrder" : ["PD", "Fighters", "Missiles", "NoFighters"] (or similar)
                """.trimMargin(), e
            )

            cycleOrder = buildCycleOrder(Values.DEFAULT_CYCLE_ORDER)
        }

        // misc
        try {
            settings?.apply {
                customAIRecursionLevel = getInt(Values.SETTINGS_AI_RECURSION_LEVEL)
                forceCustomAI = get(Values.SETTINGS_FORCE_CUSTOM_AI) == true
            }
            forceCustomAI = forceCustomAI && enableCustomAI
        } catch (e: JSONException) {
            Global.getLogger(this.javaClass).warn(
                """Error when reading misc values from settings. Please double check keys and values!
                    |Falling back to default values.
                """.trimMargin(), e
            )
        }

    }

    private fun modifyFighterAndMissileModeDescriptionsToIncludeAIType() {
        val postfix = if (enableCustomAI) {
            if (forceCustomAI) {
                "(override AI)"
            } else {
                "(custom AI)"
            }
        } else {
            "(base AI)"
        }
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.FIGHTER] += postfix
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.MISSILE] += postfix
    }

}