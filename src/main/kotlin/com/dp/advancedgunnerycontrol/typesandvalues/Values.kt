package com.dp.advancedgunnerycontrol.typesandvalues

object Values {
    const val SETTINGS_FILE_NAME = "Settings.editme"
    const val WEAPON_BLACKLIST_KEY = "weaponBlacklist"
    const val SUGGESTED_TAGS_KEY = "suggestedWeaponTags"
    const val THIS_MOD_NAME = "AdvancedGunneryControl"
    const val CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY = "AGC_Tags"
    const val CUSTOM_SHIP_DATA_SHIP_MODES_KEY = "AGC_ShipTags"
    const val CUSTOM_SHIP_DATA_SHIP_AI_KEY = "AGC_ShipAI"
    const val CUSTOM_ENGINE_TAGS_KEY = "AGC_AutoAdvanceTags"
    const val CUSTOM_SHIP_DATA_OPTIONS_TO_APPLY_KEY = "AGC_ApplyCustomOptions"
    const val CUSTOM_SHIP_DATA_SHIP_MODES_TO_APPLY_KEY = "AGC_ApplyCustomShipModes"
    const val CUSTOM_SHIP_DATA_OPTIONS_HAVE_BEEN_APPLIED_KEY = "AGC_CustomOptionsHaveBeenApplied"
    const val CUSTOM_ENGINE_AGC_PRESENT_KEY = "AGC_Present"
    const val distToAngularDistEvaluationFactor = 1f / 400f
    const val CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME = "AGC_customSuggestedTags.json"
    const val CUSTOM_SHIP_DATA_ARE_WEAPONS_MERGED_KEY = "AGC_WeaponsMerged"
    var storageIndex = 0
    const val HELP_TEXT =
        "Click on any of the top-row buttons to perform that action. Click on any mode button to enable/disable that mode." +
                "\n---Fire Mode Tags---\nFire mode tags will modify what the weapon group targets and whether" +
                " it will fire or not. Each tag will modify the behavior in a certain way. You can then combine multiple tags" +
                "\nin order to create the desired behavior. For instance, if you wish for your weapon group to only shoot at" +
                " missiles, assign the PD and NoFighters tags." +
                "\nTags that are incompatible to currently selected tags will be disabled." +
                "\nEach additional tag will make it less likely for the weapons to fire. Only when all tags agree that" +
                " a target should be shot at, the weapons will fire." +
                "\nWith very few exceptions, modes will only affect the autofire AI of that weapon. The Ship AI won't necessarily" +
                " understand that the weapon now uses non-default behavior." +
                "\n---Ship AI Modes---\nThese will modify the behavior of the ship AI. They will behave like the normal ship AI, except" +
                " for the stated modifications." +
                "\nAs the name implies, Ship AI modes will only work for AI-controlled ships, not the player controlled ship." +
                "\n---Loadouts---\nIf you want to be able to adapt your strategy based on the situation you face, you can define" +
                " multiple loadouts for your ships.\nYou can define different modes for your ships per loadout and cycle through them" +
                " during combat. All modes are saved on a per-loadout basis. " +
                "\nAs you can only cycle loadouts for all ships, make sure your loadouts fit a theme and are consistent between ships." +
                "\n---Tips---" +
                "\n - Tags are mainly a flux management tool. If your ship is already flux-neutral, you probably won't need many tags." +
                "\n - If you want a weapon group to fire as much as possible, give it no tags." +
                " In my experience, no tags or just the Flx<90% tag is the right choice for many weapon groups" +
                "\n - Consider leaving one loadout blank (all default) to give you a fallback option." +
                "\n - Be very careful with ship modes! Force disabling shields might sound cool until you run into a HIL." +
                "\n - Most values/thresholds can be adjusted in Settings.editme, if you don't like the default values."
}