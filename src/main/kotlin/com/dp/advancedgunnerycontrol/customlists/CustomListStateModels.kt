package com.dp.advancedgunnerycontrol.customlists

enum class WeaponTagListMode(val storageId: String, val displayName: String) {
    CUSTOM_GLOBAL("custom_global", "All Ships"),
    CUSTOM("custom", "This Ship"),
    NOVICE("novice", "Novice"),
    CLASSIC("classic", "Classic"),
    COMPLETE("complete", "Complete");

    val isCustom: Boolean
        get() = this == CUSTOM || this == CUSTOM_GLOBAL

    companion object {
        // Unknown persisted values fall back to the legacy per-ship custom
        // list. New ships with no state still default through
        // getActiveModeOrDefault(...) to All Ships.
        fun fromStorageId(storageId: String?): WeaponTagListMode =
            entries.firstOrNull { it.storageId == storageId } ?: CUSTOM
    }
}

data class CustomWeaponTagListState(
    var schemaVersion: Int = CustomWeaponTagListStore.SCHEMA_VERSION,
    var activeMode: String = WeaponTagListMode.CUSTOM_GLOBAL.storageId,
    var customTags: MutableList<String> = mutableListOf()
) {
    fun activeModeValue(): WeaponTagListMode = WeaponTagListMode.fromStorageId(activeMode)
}

data class CustomWeaponTagImportResult(
    val requestedTags: List<String>,
    val importedTags: List<String>,
    val unsupportedTags: List<String>,
    val activeModeBefore: WeaponTagListMode,
    val activeModeAfter: WeaponTagListMode,
    val customTagsAfter: List<String>
) {
    val changedMode: Boolean
        get() = activeModeBefore != activeModeAfter
}

data class CustomShipModeListState(
    var schemaVersion: Int = CustomShipModeListStore.SCHEMA_VERSION,
    // Legacy field kept for campaign save compatibility. Ship-mode visibility
    // now follows CustomWeaponTagListStore.activeMode instead of this value.
    var activeMode: String = WeaponTagListMode.CUSTOM.storageId,
    var customModes: MutableList<String> = mutableListOf()
)
