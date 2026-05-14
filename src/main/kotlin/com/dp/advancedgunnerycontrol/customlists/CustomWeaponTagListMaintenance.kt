package com.dp.advancedgunnerycontrol.customlists

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames

fun removeTagsFromAllShipLoadouts(
    context: ShipEditorPersistenceContext,
    tagsToRemove: Collection<String>,
): Int {
    val removals = canonicalizeWeaponTagNames(tagsToRemove.toList()).toSet()
    if (removals.isEmpty()) return 0
    var changedGroups = 0
    for (loadoutIndex in 0 until Settings.maxLoadouts()) {
        for (groupIndex in 0 until Values.MAX_WEAPON_GROUPS) {
            val currentTags = context.loadWeaponTags(groupIndex, loadoutIndex)
            val canonicalTags = canonicalizeWeaponTagNames(currentTags)
            val filteredTags = canonicalTags.filterNot { it in removals }
            if (filteredTags != canonicalTags) {
                context.saveWeaponTags(groupIndex, loadoutIndex, filteredTags)
                changedGroups++
            }
        }
    }
    return changedGroups
}

fun removeTagsOutsideVisibleListFromAllShipLoadouts(
    context: ShipEditorPersistenceContext,
    visibleTags: Collection<String>,
): Int {
    val visible = canonicalizeWeaponTagNames(visibleTags.toList()).toSet()
    var changedGroups = 0
    for (loadoutIndex in 0 until Settings.maxLoadouts()) {
        for (groupIndex in 0 until Values.MAX_WEAPON_GROUPS) {
            val currentTags = context.loadWeaponTags(groupIndex, loadoutIndex)
            val canonicalTags = canonicalizeWeaponTagNames(currentTags)
            val filteredTags = canonicalTags.filter { it in visible }
            if (filteredTags != canonicalTags) {
                context.saveWeaponTags(groupIndex, loadoutIndex, filteredTags)
                changedGroups++
            }
        }
    }
    return changedGroups
}
