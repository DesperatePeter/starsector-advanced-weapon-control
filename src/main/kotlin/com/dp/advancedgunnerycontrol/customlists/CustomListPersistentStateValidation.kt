package com.dp.advancedgunnerycontrol.customlists

@Suppress("UNCHECKED_CAST")
internal fun Any?.validCustomWeaponTagStatesOrNull(): MutableMap<String, CustomWeaponTagListState>? {
    val map = this as? MutableMap<*, *> ?: return null
    val normalized = linkedMapOf<String, CustomWeaponTagListState>()
    var replacedLegacyState = false
    for ((key, value) in map) {
        if (key !is String) return null
        val state = value.toCurrentCustomWeaponTagListStateOrNull() ?: return null
        val customTags = state.customTags as? List<*> ?: return null
        if (!customTags.all { it is String }) return null
        normalized[key] = state
        replacedLegacyState = replacedLegacyState || value !is CustomWeaponTagListState
    }
    if (replacedLegacyState) {
        val mutableMap = map as MutableMap<Any?, Any?>
        mutableMap.clear()
        normalized.forEach { (key, value) -> mutableMap[key] = value }
    }
    return map as MutableMap<String, CustomWeaponTagListState>
}

@Suppress("UNCHECKED_CAST")
internal fun Any?.validCustomShipModeStatesOrNull(): MutableMap<String, CustomShipModeListState>? {
    val map = this as? MutableMap<*, *> ?: return null
    val normalized = linkedMapOf<String, CustomShipModeListState>()
    var replacedLegacyState = false
    for ((key, value) in map) {
        if (key !is String) return null
        val state = value.toCurrentCustomShipModeListStateOrNull() ?: return null
        val customModes = state.customModes as? List<*> ?: return null
        if (!customModes.all { it is String }) return null
        normalized[key] = state
        replacedLegacyState = replacedLegacyState || value !is CustomShipModeListState
    }
    if (replacedLegacyState) {
        val mutableMap = map as MutableMap<Any?, Any?>
        mutableMap.clear()
        normalized.forEach { (key, value) -> mutableMap[key] = value }
    }
    return map as MutableMap<String, CustomShipModeListState>
}

private fun Any?.toCurrentCustomWeaponTagListStateOrNull(): CustomWeaponTagListState? {
    return when (this) {
        is CustomWeaponTagListState -> this
        is com.dp.advancedgunnerycontrol.typesandvalues.CustomWeaponTagListState ->
            CustomWeaponTagListState(
                schemaVersion = schemaVersion,
                activeMode = activeMode,
                customTags = customTags.toMutableList()
            )
        else -> null
    }
}

private fun Any?.toCurrentCustomShipModeListStateOrNull(): CustomShipModeListState? {
    return when (this) {
        is CustomShipModeListState -> this
        is com.dp.advancedgunnerycontrol.typesandvalues.CustomShipModeListState ->
            CustomShipModeListState(
                schemaVersion = schemaVersion,
                activeMode = activeMode,
                customModes = customModes.toMutableList()
            )
        else -> null
    }
}
