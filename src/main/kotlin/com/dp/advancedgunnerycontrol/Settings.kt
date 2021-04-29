package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode
import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.getFloat
import java.io.IOException
typealias Settings = AdvancedGunneryControlSettings

class AdvancedGunneryControlSettings {
    companion object {
        var settings: JSONObject? = null
        var cycleOrder = buildCycleOrder(Values.DEFAULT_CYCLE_ORDER)
            private set
        var enableCustomAI = Values.DEFAULT_ENABLE_CUSTOM_AI
            private set
        var customAIRecursionLevel = Values.DEFAULT_AI_RECURSION_LEVEL
            private set
        var forceCustomAI = Values.DEFAULT_FORCE_CUSTOM_AI
            private set
        var customAITriggerHappiness = Values.DEFAULT_AI_TRIGGER_HAPPINESS
            private set
        var customAIPerfectTargetLeading = Values.DEFAULT_AI_PERFECT_TARGET_LEADING
            private set
        var customAIFriendlyFireCaution = Values.DEFAULT_AI_FRIENDLY_FIRE_CAUTION
            private set
        var isFallbackToDefault = false
            private set

        fun buildCycleOrder(additionalItems: List<String>): List<FireMode> {
            return (listOf(FireMode.DEFAULT) + additionalItems.mapNotNull {
                Values.FIRE_MODE_TRANSLATIONS[it]
            })
        }
    }

        fun loadSettings() {
            readSettings()
            applySettings()
        }

        private fun readSettings() {
            try {
                Global.getSettings().loadJSON(Values.SETTINGS_FILE_NAME).also { settings = it }
            } catch (e: IOException) {
                isFallbackToDefault = true
                Global.getLogger(this.javaClass).warn(
                    "Unable to load settings file settings.editme! Falling back to default settings", e
                )
            } catch (e: JSONException) {
                isFallbackToDefault = true
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
                isFallbackToDefault = true
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
                isFallbackToDefault = true
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
                    customAIRecursionLevel = getInt(Values.SETTINGS_AI_RECURSION_LEVEL_KEY)
                    forceCustomAI = get(Values.SETTINGS_FORCE_CUSTOM_AI_KEY) == true
                    customAITriggerHappiness = getFloat(Values.SETTINGS_CUSTOM_AI_TRIGGER_HAPPINESS_KEY)
                    customAIPerfectTargetLeading = get(Values.SETTINGS_USE_PERFECT_TARGET_LEADING_KEY) == true
                    customAIFriendlyFireCaution = getFloat(Values.SETTINGS_AI_FRIENDLY_FIRE_CAUTION_KEY)
                }
                forceCustomAI = forceCustomAI && enableCustomAI
            } catch (e: JSONException) {
                isFallbackToDefault = true
                Global.getLogger(this.javaClass).warn(
                    """Error when reading misc values from settings. Please double check keys and values!
                    |Falling back to default values.
                """.trimMargin(), e
                )
            }

        }

}