package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode

class Values {
    companion object{
        const val SETTINGS_FILE_NAME = "Settings.editme"
        const val SETTINGS_CYCLE_ORDER_KEY = "cycleOrder"
        const val SETTINGS_ENABLE_CUSTOM_AI_KEY = "enableCustomAI"
        const val SETTINGS_AI_RECURSION_LEVEL_KEY = "customAIRecursionLevel"
        const val SETTINGS_FORCE_CUSTOM_AI_KEY = "forceCustomAI"
        const val SETTINGS_CUSTOM_AI_TRIGGER_HAPPINESS_KEY = "customAITriggerHappiness"
        const val SETTINGS_USE_PERFECT_TARGET_LEADING_KEY = "customAIAlwaysUsesBestTargetLeading"
        const val SETTINGS_AI_FRIENDLY_FIRE_CAUTION_KEY = "customAIFriendlyFireCaution"
        const val SETTINGS_AI_FRIENDLY_FIRE_COMPLEXITY_KEY = "customAIFriendlyFireAlgorithmComplexity"
        const val WEAPON_BLACKLIST_KEY = "weaponBlacklist"
        const val THIS_MOD_NAME = "AdvancedGunneryControl"
        val DEFAULT_CYCLE_ORDER = listOf("PD", "Fighters", "Missiles", "NoFighters")
        const val DEFAULT_ENABLE_CUSTOM_AI = true
        const val DEFAULT_AI_RECURSION_LEVEL = 1
        const val DEFAULT_FORCE_CUSTOM_AI = false
        const val DEFAULT_AI_TRIGGER_HAPPINESS = 2.0f
        const val DEFAULT_AI_PERFECT_TARGET_LEADING = false
        const val DEFAULT_AI_FRIENDLY_FIRE_CAUTION = 1.0f
        const val DEFAULT_AI_FRIENDLY_FIRE_COMPLEXITY = 1
        val FIRE_MODE_TRANSLATIONS = mapOf(
            "Default" to FireMode.DEFAULT,
            "PD" to FireMode.PD,
            "Fighters" to FireMode.FIGHTER,
            "Missiles" to FireMode.MISSILE,
            "NoFighters" to FireMode.NO_FIGHTERS
        )
        var FIRE_MODE_DESCRIPTIONS = mutableMapOf(
            FireMode.DEFAULT to "Default (base AI)",
            FireMode.PD to "PD Mode (base AI)",
            FireMode.FIGHTER to "Fighters only",
            FireMode.MISSILE to "Missiles only",
            FireMode.NO_FIGHTERS to "Ignore Fighters (base AI)"
        )
        const val distToAngularDistEvalutionFactor = 1f/1000f
    }
}