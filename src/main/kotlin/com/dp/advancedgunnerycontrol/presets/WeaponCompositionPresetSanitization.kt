package com.dp.advancedgunnerycontrol.presets

import com.dp.advancedgunnerycontrol.customlists.CustomWeaponTagListStore
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.dp.advancedgunnerycontrol.weapontags.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.weapontags.shouldTagBeDisabled
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal data class PresetTagSanitizationResult(
    val tags: List<String>,
    val importedCustomTags: List<String> = emptyList(),
    val unsupportedCustomTags: List<String> = emptyList(),
)

fun sanitizeWeaponCompositionPresetTagsForGroup(
    member: FleetMemberAPI,
    groupIndex: Int,
    tags: List<String>
): List<String> = sanitizeTagsForWeaponGroup(member, groupIndex, tags)

internal fun sanitizeTagsForWeaponGroup(
    member: FleetMemberAPI,
    groupIndex: Int,
    tags: List<String>,
    runtimeShip: ShipAPI? = null,
    importSupportedCustomTags: Boolean = false,
): List<String> = sanitizePresetTagsForWeaponGroup(
    member = member,
    groupIndex = groupIndex,
    tags = tags,
    runtimeShip = runtimeShip,
    importSupportedCustomTags = importSupportedCustomTags
).tags

internal fun sanitizePresetTagsForWeaponGroup(
    member: FleetMemberAPI,
    groupIndex: Int,
    tags: List<String>,
    runtimeShip: ShipAPI? = null,
    importSupportedCustomTags: Boolean = false,
): PresetTagSanitizationResult {
    val canonicalTags = canonicalizeWeaponTagNames(tags)
    val shipId = ShipEditorPersistenceContext(member, runtimeShip).shipId
    val defaultTags = Settings.getCurrentWeaponTagList()
    val currentVisibleTags = if (Settings.isAdvancedMode) {
        CustomWeaponTagListStore.effectiveTagsForShipOrDefault(shipId, defaultTags)
    } else {
        defaultTags
    }
    var importedCustomTags = emptyList<String>()
    var unsupportedCustomTags = emptyList<String>()
    if (importSupportedCustomTags && Settings.isAdvancedMode) {
        // Preset loads may contain exact editable/custom tags that are not in the
        // current visible list yet. Import only those missing valid tags so a
        // normal preset load does not switch the ship to Custom unnecessarily.
        val missingTags = canonicalTags.filterNot { currentVisibleTags.contains(it) }
        if (missingTags.isNotEmpty()) {
            val importResult = CustomWeaponTagListStore.importValidTagsIntoCustomList(
                shipId,
                missingTags,
                switchToCustom = true
            )
            importedCustomTags = importResult.importedTags
            unsupportedCustomTags = importResult.unsupportedTags
            if (importedCustomTags.isNotEmpty()) {
                // Preset imports must also refresh the session tag registry;
                // older combat/refit paths still consult Settings tag lists.
                Settings.hotAddTags(importedCustomTags)
            }
        }
    }
    val allVisibleTags = if (Settings.isAdvancedMode) {
        CustomWeaponTagListStore.effectiveTagsForShipOrDefault(shipId, defaultTags)
    } else {
        defaultTags
    }
    val sanitized = canonicalizeWeaponTagNames(tags)
        .filter { allVisibleTags.contains(it) }
        .toMutableList()
    var changed = true
    while (changed) {
        changed = false
        sanitized.toList().forEach { persistedTag ->
            val otherTags = sanitized.toMutableList().apply { remove(persistedTag) }
            if (isIncompatibleWithExistingTags(persistedTag, otherTags) || shouldTagBeDisabled(groupIndex, member, persistedTag)) {
                sanitized.remove(persistedTag)
                changed = true
            }
        }
    }
    return PresetTagSanitizationResult(
        tags = sanitized,
        importedCustomTags = importedCustomTags,
        unsupportedCustomTags = unsupportedCustomTags,
    )
}
