package com.dp.advancedgunnerycontrol.presets

import com.dp.advancedgunnerycontrol.compat.loadLegacyDpGlobalWeaponPreset
import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

enum class WeaponPresetScope {
    SINGLE,
    CLASS,
    GLOBAL,
    SUGGESTED;

    fun label(): String = when (this) {
        SINGLE -> "Single"
        CLASS -> "Class"
        GLOBAL -> "Global"
        SUGGESTED -> "Suggested"
    }

    fun next(): WeaponPresetScope {
        val values = values()
        return values[(ordinal + 1) % values.size]
    }
}

enum class WeaponPresetBackend {
    CAMPAIGN,
    EXTERNAL;

    fun label(): String = when (this) {
        CAMPAIGN -> "Campaign"
        EXTERNAL -> "External"
    }
}

enum class WeaponCompositionPresetSaveStatus {
    SAVED,
    NO_WEAPON_GROUP_KEY,
    SUGGESTED_REQUIRES_SINGLE_WEAPON,
    FAILED
}

enum class WeaponCompositionPresetLoadStatus {
    LOADED,
    NO_WEAPON_GROUP_KEY,
    NO_PRESET_FOUND,
    FAILED
}

enum class WeaponCompositionPresetPeekStatus {
    FOUND,
    NO_WEAPON_GROUP_KEY,
    NO_PRESET_FOUND,
    FAILED
}

data class WeaponCompositionPresetSaveResult(
    val status: WeaponCompositionPresetSaveStatus,
    val weaponKey: String? = null,
    val tags: List<String> = emptyList(),
    val overwrittenGroups: Int = 0,
    val error: String? = null
)

data class WeaponCompositionPresetLoadResult(
    val status: WeaponCompositionPresetLoadStatus,
    val weaponKey: String? = null,
    val tags: List<String> = emptyList(),
    val importedCustomTags: List<String> = emptyList(),
    val error: String? = null
)

data class WeaponCompositionPresetPeekResult(
    val status: WeaponCompositionPresetPeekStatus,
    val weaponKey: String? = null,
    val tags: List<String> = emptyList(),
    val error: String? = null
)

fun saveExternalWeaponCompositionPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
    runtimeShip: ShipAPI? = null
): WeaponCompositionPresetSaveResult {
    return saveWeaponPreset(
        member = member,
        groupIndex = groupIndex,
        loadoutIndex = loadoutIndex,
        scope = WeaponPresetScope.GLOBAL,
        backend = WeaponPresetBackend.EXTERNAL,
        overwrite = false,
        file = file,
        runtimeShip = runtimeShip
    )
}

fun saveWeaponPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    scope: WeaponPresetScope,
    backend: WeaponPresetBackend,
    overwrite: Boolean,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
    runtimeShip: ShipAPI? = null,
    persistenceContext: ShipEditorPersistenceContext? = null,
): WeaponCompositionPresetSaveResult {
    val key = storageKey(member, groupIndex, scope, runtimeShip)
        ?: return if (scope == WeaponPresetScope.SUGGESTED) {
            WeaponCompositionPresetSaveResult(WeaponCompositionPresetSaveStatus.SUGGESTED_REQUIRES_SINGLE_WEAPON)
        } else {
            WeaponCompositionPresetSaveResult(WeaponCompositionPresetSaveStatus.NO_WEAPON_GROUP_KEY)
    }
    return try {
        val context = persistenceContext ?: ShipEditorPersistenceContext(member, runtimeShip)
        val selectedTags = context.loadWeaponTags(groupIndex, loadoutIndex)
        val sanitized = sanitizeTagsForWeaponGroup(member, groupIndex, selectedTags)

        val overwriteAllLoadouts = overwrite && scope != WeaponPresetScope.SUGGESTED
        val presetLoadouts = if (overwriteAllLoadouts) {
            (0 until Settings.maxLoadouts()).toList()
        } else {
            listOf(loadoutIndex)
        }
        val store = presetStore(backend, file)
        presetLoadouts.forEach { targetLoadoutIndex ->
            store.put(presetLoadoutIndex(scope, targetLoadoutIndex), key, sanitized)
        }
        val overwritten = if (overwrite && scope != WeaponPresetScope.SUGGESTED) {
            overwriteMatchingGroups(member, groupIndex, presetLoadouts, scope, sanitized, runtimeShip)
        } else {
            0
        }

        WeaponCompositionPresetSaveResult(
            status = WeaponCompositionPresetSaveStatus.SAVED,
            weaponKey = key,
            tags = sanitized,
            overwrittenGroups = overwritten
        )
    } catch (ex: Exception) {
        logPresetWarn("Failed saving weapon preset for key=$key, loadout=$loadoutIndex, scope=$scope, backend=$backend", ex)
        WeaponCompositionPresetSaveResult(
            status = WeaponCompositionPresetSaveStatus.FAILED,
            weaponKey = key,
            error = ex.message
        )
    }
}

fun loadExternalWeaponCompositionPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
    runtimeShip: ShipAPI? = null
): WeaponCompositionPresetLoadResult {
    return loadWeaponPreset(
        member = member,
        groupIndex = groupIndex,
        loadoutIndex = loadoutIndex,
        scope = WeaponPresetScope.GLOBAL,
        backend = WeaponPresetBackend.EXTERNAL,
        file = file,
        runtimeShip = runtimeShip
    )
}

fun loadWeaponPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    scope: WeaponPresetScope,
    backend: WeaponPresetBackend,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
    runtimeShip: ShipAPI? = null,
    persistenceContext: ShipEditorPersistenceContext? = null,
): WeaponCompositionPresetLoadResult {
    val key = storageKey(member, groupIndex, scope, runtimeShip)
    val loadKeys = if (scope == WeaponPresetScope.SUGGESTED) {
        suggestedLoadKeys(member, groupIndex)
    } else {
        key?.let(::listOf)
    } ?: return WeaponCompositionPresetLoadResult(WeaponCompositionPresetLoadStatus.NO_WEAPON_GROUP_KEY)
    return try {
        val store = presetStore(backend, file)
        val loadedEntries = loadKeys.mapNotNull { store.get(presetLoadoutIndex(scope, loadoutIndex), it) }
        val savedTags = if (loadedEntries.isNotEmpty()) {
            loadedEntries.flatten()
        } else if (scope == WeaponPresetScope.GLOBAL && backend == WeaponPresetBackend.EXTERNAL && key != null) {
            store.get(loadoutIndex, key.removePrefix(GLOBAL_SCOPE_PREFIX))
                ?: loadLegacyDpGlobalWeaponPreset(
                    member = member,
                    groupIndex = groupIndex,
                    runtimeShip = runtimeShip,
                    primaryFile = file,
                )
        } else {
            null
        }
        if (savedTags == null) {
            return WeaponCompositionPresetLoadResult(
                status = WeaponCompositionPresetLoadStatus.NO_PRESET_FOUND,
                weaponKey = key ?: loadKeys.joinToString(",")
            )
        }

        val sanitized = sanitizePresetTagsForWeaponGroup(
            member = member,
            groupIndex = groupIndex,
            tags = savedTags,
            runtimeShip = runtimeShip,
            importSupportedCustomTags = true
        )
        if (sanitized.unsupportedCustomTags.isNotEmpty()) {
            logPresetWarn(
                "Ignored unsupported custom preset tags for key=$key, loadout=$loadoutIndex: " +
                    sanitized.unsupportedCustomTags.joinToString(", ")
            )
        }
        (persistenceContext ?: ShipEditorPersistenceContext(member, runtimeShip))
            .saveWeaponTags(groupIndex, loadoutIndex, sanitized.tags)

        WeaponCompositionPresetLoadResult(
            status = WeaponCompositionPresetLoadStatus.LOADED,
            weaponKey = key,
            tags = sanitized.tags,
            importedCustomTags = sanitized.importedCustomTags,
        )
    } catch (ex: Exception) {
        logPresetWarn("Failed loading weapon preset for key=$key, loadout=$loadoutIndex, scope=$scope, backend=$backend", ex)
        WeaponCompositionPresetLoadResult(
            status = WeaponCompositionPresetLoadStatus.FAILED,
            weaponKey = key,
            error = ex.message
        )
    }
}

fun peekExternalWeaponCompositionPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME
): WeaponCompositionPresetPeekResult {
    return peekWeaponPreset(member, groupIndex, loadoutIndex, WeaponPresetScope.GLOBAL, WeaponPresetBackend.EXTERNAL, file)
}

fun peekWeaponPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    loadoutIndex: Int,
    scope: WeaponPresetScope,
    backend: WeaponPresetBackend,
    file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
): WeaponCompositionPresetPeekResult {
    val key = storageKey(member, groupIndex, scope)
    val loadKeys = if (scope == WeaponPresetScope.SUGGESTED) {
        suggestedLoadKeys(member, groupIndex)
    } else {
        key?.let(::listOf)
    } ?: return if (scope == WeaponPresetScope.SUGGESTED) {
        WeaponCompositionPresetPeekResult(WeaponCompositionPresetPeekStatus.NO_PRESET_FOUND)
    } else {
        WeaponCompositionPresetPeekResult(WeaponCompositionPresetPeekStatus.NO_WEAPON_GROUP_KEY)
    }
    return try {
        val store = presetStore(backend, file)
        val loadedEntries = loadKeys.mapNotNull { store.get(presetLoadoutIndex(scope, loadoutIndex), it) }
        val savedTags = if (loadedEntries.isNotEmpty()) {
            loadedEntries.flatten()
        } else if (scope == WeaponPresetScope.GLOBAL && backend == WeaponPresetBackend.EXTERNAL && key != null) {
            store.get(loadoutIndex, key.removePrefix(GLOBAL_SCOPE_PREFIX))
                ?: loadLegacyDpGlobalWeaponPreset(
                    member = member,
                    groupIndex = groupIndex,
                    primaryFile = file,
                )
        } else {
            null
        }
            ?: return WeaponCompositionPresetPeekResult(
                status = WeaponCompositionPresetPeekStatus.NO_PRESET_FOUND,
                weaponKey = key ?: loadKeys.joinToString(",")
            )
        WeaponCompositionPresetPeekResult(
            status = WeaponCompositionPresetPeekStatus.FOUND,
            weaponKey = key ?: loadKeys.joinToString(","),
            tags = sanitizeTagsForWeaponGroup(member, groupIndex, savedTags)
        )
    } catch (ex: Exception) {
        logPresetWarn("Failed peeking weapon preset for key=$key, loadout=$loadoutIndex, scope=$scope, backend=$backend", ex)
        WeaponCompositionPresetPeekResult(
            status = WeaponCompositionPresetPeekStatus.FAILED,
            weaponKey = key,
            error = ex.message
        )
    }
}
