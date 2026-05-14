package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.shipdata.loadPersistentTags
import com.dp.advancedgunnerycontrol.shipdata.persistTags
import com.fs.starfarer.api.fleet.FleetMemberAPI

fun applySuggestedWeaponTags(ship: FleetMemberAPI, storageIndex: Int, allowOverriding: Boolean = true, shipId: String? = null) {
    val id = shipId ?: agcStableShipId(ship)
    val groups = ship.variant.weaponGroups

    groups.forEachIndexed { index, group ->
        if(allowOverriding || loadPersistentTags(id, index, storageIndex).isEmpty()){
            val weaponID = group.slots?.firstOrNull()?.let { ship.variant.getWeaponId(it) } ?: ""
            persistTags(id, index, storageIndex, getSuggestedTagsForWeaponId(weaponID))
        }
    }
}

fun getSuggestedTagsForWeaponId(weaponID: String) : List<String>{
    return getSuggestedTagsForWeaponId(weaponID, Settings.getCurrentWeaponTagList().toSet())
}

fun getSuggestedTagsForWeaponId(weaponID: String, supportedTags: Set<String>) : List<String>{
    val suggestedTags = Settings.getCurrentSuggestedTags()
    val rawTags = suggestedTags[weaponID]
        ?: suggestedTags.entries.firstOrNull { (key, _) ->
            runCatching { Regex(key).matches(weaponID) }.getOrDefault(false)
        }?.value
        ?: emptyList()
    return rawTags
        .map(::canonicalizeWeaponTagName)
        .filter { it in supportedTags }
        .distinct()
}
