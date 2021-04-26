package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.iterator
import java.io.IOException


public class WeaponControlBasePlugin : BaseModPlugin() {
    var settings: JSONObject? = null

    // TODO: Migrate to separate class?
    companion object Settings {
        var cycleOrder = buildCycleOrder(Values.DEFAULT_CYCLE_ORDER)
            private set
        var enableCustomAI = Values.DEFAULT_ENABLE_CUSTOM_AI
        fun buildCycleOrder(additionalItems : List<String>) : List<FireMode>{
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
        Global.getLogger(this.javaClass).info("Using custom AI: $enableCustomAI" )
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
        try {
            settings?.let { enableCustomAI = (it.get(Values.SETTINGS_ENABLE_CUSTOM_AI_KEY) == true) }
        }catch (e: JSONException){
            Global.getLogger(this.javaClass).warn(
                """Key 'enableCustomAI' in ${Values.SETTINGS_FILE_NAME} invalid or not present.
                    | Entry should be: "enableCustomAI" : true/false """.trimMargin(), e
            )
        }

        try {
            settings?.let { config ->
                val array = config.getJSONArray(Values.SETTINGS_CYCLE_ORDER_KEY)
                var modesToAdd  = mutableListOf<String>()
                for (i in 0 until array.length()){ // forEach fails for some reason, so...manual for-loop...
                    modesToAdd.add(array.getString(i))
                }
                cycleOrder = buildCycleOrder(modesToAdd)
            }
        }catch (e: JSONException){
            Global.getLogger(this.javaClass).warn(
                """Key "cycleOrder" in ${Values.SETTINGS_FILE_NAME} not present or content invalid.
                    |Entry should be: "cycleOrder" : ["PD", "Fighters", "Missiles", "NoFighters"] (or similar)
                """.trimMargin(), e)

            cycleOrder = buildCycleOrder(Values.DEFAULT_CYCLE_ORDER)
        }
    }

    private fun modifyFighterAndMissileModeDescriptionsToIncludeAIType(){
        val postfix = if (enableCustomAI){
            "(custom AI)"
        }else{
            "(vanilla AI)"
        }
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.FIGHTER]+=postfix
        Values.FIRE_MODE_DESCRIPTIONS[FireMode.MISSILE]+=postfix
    }

}