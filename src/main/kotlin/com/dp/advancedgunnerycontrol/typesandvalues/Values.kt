package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.settings.Settings

object Values {
    const val SETTINGS_FILE_NAME = "Settings.editme"
    const val WEAPON_BLACKLIST_KEY = "weaponBlacklist"
    const val SUGGESTED_TAGS_KEY = "suggestedWeaponTags"
    const val THIS_MOD_NAME = "AdvancedGunneryControl"
    const val CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY = "AGC_Tags"
    const val CUSTOM_SHIP_DATA_SHIP_MODES_KEY = "AGC_ShipTags"
    const val distToAngularDistEvaluationFactor = 1f / 400f
    var storageIndex = 0
    val HELP_TEXT = "---Ship AI Modes---\nThese will modify the behavior of the ship AI. They will behave like the normal ship AI, except" +
            " for the stated modifications." +
            "\nAs the name implies, Ship AI modes will only work for AI-controlled ships, not the player controlled ship." +
            "\n---Fire Mode Tags---\nFire mode tags will modify what the weapon group targets and whether" +
            " it will fire or not. Each tag will modify the behavior in a certain way. You can then combine multiple tags" +
            "\nin order to create the desired behavior. For instance, if you wish for your weapon to target only shielded" +
            " fighters, you could give it the Fighter and TargetShields tags." +
            "\nNote: Each additional tag will make it less likely for the weapons to fire. Only when all tags agree that" +
            " a target is valid, the weapons will fire." +
            "\n---Loadouts---\nIf you want to be able to adapt your strategy based on the situation you face, you can define" +
            " multiple loadouts for your ships.\nYou can define different modes for your ships per loadout and cycle through them" +
            " during combat." +
            "\nAs you can only cycle loadouts for all ships, make sure your loadouts fit a theme and are consistent between ships." +
            "\n---Tips---\n - If you want a weapon group to fire as much as possible, give it no tags." +
            " In my experience, no tags or just the Flx<90% tag is the right choice for most weapon groups" +
            "\n - Consider leaving one loadout blank (all default) to give you a fallback option." +
            "\n - Be very careful with ship modes! Force disabling shields might sound cool until you run into a HIL." +
            "\n - Most values/thresholds can be adjusted in Settings.editme, if you don't like the default values."
}