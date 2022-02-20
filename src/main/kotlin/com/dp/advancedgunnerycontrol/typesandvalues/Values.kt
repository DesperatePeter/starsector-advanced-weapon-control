package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings

object Values {
    const val SETTINGS_FILE_NAME = "Settings.editme"
    const val WEAPON_BLACKLIST_KEY = "weaponBlacklist"
    const val SUGGESTED_WEAPON_MODES_KEY = "suggestedWeaponModes"
    const val SUGGESTED_WEAPON_SUFFIXES_KEY = "suggestedWeaponSuffixes"
    const val THIS_MOD_NAME = "AdvancedGunneryControl"
    const val WEAPON_AI_MANAGER_KEY = "WeaponAIManagerAGC"
    const val CUSTOM_SHIP_DATA_TAG_KEY = "AGC_Tags"
    const val distToAngularDistEvaluationFactor = 1f / 400f
    val HELP_TEXT = "---Ship AI Modes---\nThese will modify the behavior of the ship AI. They will behave like the normal ship AI, except" +
            " for the stated modifications. Note that, unless you use the ForceAutofire ship mode, AI-controlled ships won't" +
            " follow the configured modes all the time, as the ship AI will manually control/fire weapon groups." +
            "\nAs the name implies, Ship AI modes will only work for AI-controlled ships, not the player controlled ship." +
            "\nIf you set the player-controlled ship to autopilot and want to use the configured ship mode, you have to" +
            " manually load it (${Settings.infoHotkey()}-Key), as the player-controlled ship doesn't use an AI by default." +
            "\n\n---Fire Modes---\nThese are the core of this mod. They will modify what the weapon group targets and whether" +
            " it will fire or not. Given default settings, most modes will first try to find a firing solution using the" +
            " base weapon AI. Only when the base AI selects a target that doesn't match the fire mode, the custom AI will" +
            " kick in and try to find a suitable target." +
            "\nSome modes (e.g. PD-Mode) simply won't fire rather than trying to" +
            " use the custom AI (as the base AI already prioritizes missiles/fighters for PD weapons)." +
            "\nSome modes (e.g. Opportunist) skip the base AI entirely." +
            "\n\n---Suffixes---\nSuffixes allow you to further customize the behavior of fire modes. Suffixes can modify the" +
            " targeting priority (when using custom AI) and the decision whether to fire or not (both custom and base AI)." +
            "\n\n---Loadouts---\nIf you want to be able to adapt your strategy based on the situation you face, you can define" +
            " multiple loadouts for your ships. You can define different modes for your ships per loadout and cycle through them" +
            " during combat." +
            "\nAs you can only cycle loadouts for all ships, make sure your loadouts fit a theme and are consistent between ships." +
            "\nCustomize the number of available loadouts and their names in Settings.editme" +
            "\n\n---Hotkeys---" +
            "\nNote: All modifications to modes made during combat WILL be saved (by default)." +
            "\n${Settings.getKeybindingInfoText()}" +
            "\n\n---Tips---\n - Often times, default mode with no suffix (or HoldFire (Flux>90%)) is the best option." +
            "\n - Setting modes/suffixes usually makes guns fire less." +
            "\n - Observe how the AI behaves in combat and adjust modes based on that." +
            "\n - Consider leaving one loadout blank (all default) to give you a fallback option." +
            "\n - Be very careful with ship modes! Force disabling shields might sound cool until you run into a HIL." +
            "\n - Most values/thresholds can be adjusted in Settings.editme, if you don't like the default values."
}