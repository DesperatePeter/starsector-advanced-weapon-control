package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode

class Values {
    companion object{
        const val SETTINGS_FILE_NAME = "Settings.editme"
        const val SETTINGS_CYCLE_ORDER_KEY = "cycleOrder"
        const val SETTINGS_ENABLE_CUSTOM_AI_KEY = "enableCustomAI"
        const val SETTINGS_AI_RECURSION_LEVEL = "customAIRecursionLevel"
        const val SETTINGS_FORCE_CUSTOM_AI = "forceCustomAI"
        const val SETTINGS_CUSTOM_AI_TRIGGER_HAPPINESS = "customAITriggerHappiness"
        val DEFAULT_CYCLE_ORDER = listOf("PD", "Fighters", "Missiles", "NoFighters")
        const val DEFAULT_ENABLE_CUSTOM_AI = true
        const val DEFAULT_AI_RECURSION_LEVEL = 2
        const val DEFAULT_FORCE_CUSTOM_AI = false
        const val DEFAULT_CUSTOM_AI_TRIGGER_HAPPINESS = 1.0f
        val FIRE_MODE_TRANSLATIONS = mapOf(
            "Default" to FireMode.DEFAULT,
            "PD" to FireMode.PD,
            "Fighters" to FireMode.FIGHTER,
            "Missiles" to FireMode.MISSILE,
            "NoFighters" to FireMode.NO_FIGHTERS
        )
        var FIRE_MODE_DESCRIPTIONS = mutableMapOf(
            FireMode.DEFAULT to "Default",
            FireMode.PD to "PD Mode",
            FireMode.FIGHTER to "Fighters only",
            FireMode.MISSILE to "Missiles only",
            FireMode.NO_FIGHTERS to "Ignore Fighters"
        )
    }
}