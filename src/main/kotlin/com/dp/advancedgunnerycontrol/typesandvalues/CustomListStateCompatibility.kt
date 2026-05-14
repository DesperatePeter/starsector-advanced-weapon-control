package com.dp.advancedgunnerycontrol.typesandvalues

/**
 * Legacy save-compatibility classes for campaign data written before the
 * package split. Starsector/XStream resolves saved objects by fully qualified
 * class name before current code can migrate them, so these names must remain
 * loadable until old saves no longer need direct migration.
 */
data class CustomWeaponTagListState(
    var schemaVersion: Int = 1,
    var activeMode: String = "custom_global",
    var customTags: MutableList<String> = mutableListOf()
)

data class CustomShipModeListState(
    var schemaVersion: Int = 2,
    var activeMode: String = "custom",
    var customModes: MutableList<String> = mutableListOf()
)
