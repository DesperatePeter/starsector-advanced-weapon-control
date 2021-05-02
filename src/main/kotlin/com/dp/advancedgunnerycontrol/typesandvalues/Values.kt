package com.dp.advancedgunnerycontrol.typesandvalues

object Values {
    const val SETTINGS_FILE_NAME = "Settings.editme"
    const val SETTINGS_CYCLE_ORDER_KEY = "cycleOrder"
    const val SETTINGS_ENABLE_CUSTOM_AI_KEY = "enableCustomAI"
    const val SETTINGS_AI_RECURSION_LEVEL_KEY = "customAIRecursionLevel"
    const val SETTINGS_FORCE_CUSTOM_AI_KEY = "forceCustomAI"
    const val SETTINGS_CUSTOM_AI_TRIGGER_HAPPINESS_KEY = "customAITriggerHappiness"
    const val SETTINGS_USE_PERFECT_TARGET_LEADING_KEY = "customAIAlwaysUsesBestTargetLeading"
    const val SETTINGS_AI_FRIENDLY_FIRE_CAUTION_KEY = "customAIFriendlyFireCaution"
    const val SETTINGS_AI_FRIENDLY_FIRE_COMPLEXITY_KEY = "customAIFriendlyFireAlgorithmComplexity"
    const val SETTINGS_UI_DISPLAY_FRAMES = "messageDisplayDuration"
    const val SETTINGS_UI_X = "messagePositionX"
    const val SETTINGS_UI_Y = "messagePositionY"
    const val SETTINGS_FORCE_FULL_INFO = "alwaysShowFullInfo"
    const val SETTINGS_INFO_HOTKEY_KEY = "saveLoadInfoHotkey"
    const val SETTINGS_PERSISTENT_STORAGE = "enablePersistentFireModes"

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
    const val DEFAULT_UI_DISPLAY_FRAMES = 150
    const val DEFAULT_UI_X = 350
    const val DEFAULT_UI_Y = 100
    const val DEFAULT_FORCE_FULL_INFO = false
    const val DEFAULT_INFO_HOTKEY = 'j'
    const val DEFAULT_ENABLE_PERSISTENT_STORAGE = true

    const val WEAPON_AI_MANAGER_KEY = "WeaponAIManagerAGC"

    const val distToAngularDistEvalutionFactor = 1f / 1000f

}