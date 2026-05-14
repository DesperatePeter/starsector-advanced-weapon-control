package com.dp.advancedgunnerycontrol.shipdata

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.utils.InShipShipModeStorage
import com.dp.advancedgunnerycontrol.utils.InShipTagStorage
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

fun loadTags(ship: ShipAPI, index: Int, storageIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistence() || !doesShipHaveLocalTags(ship, storageIndex)) {
        val shipId = agcStableShipId(ship)
        if (shipId.isBlank()) return emptyList()
        return loadPersistentTags(shipId, index, storageIndex)
    }
    return loadTagsFromShip(ship, index, storageIndex)
}

/**
 * loads all tag types that have been used for a given ship.
 * the returned list won't contain duplicates
 * this is used to hot-load tags when viewing a ship in GUI
 */
fun loadAllTags(ship: FleetMemberAPI, universalId: String? = null): List<String> {
    val tags = mutableSetOf<String>()
    val shipId = universalId ?: agcStableShipId(ship)
    for (loadoutIndex in 0 until Settings.maxLoadouts()) {
        for (groupIndex in 0 until Values.MAX_WEAPON_GROUPS) {
            tags.addAll(loadPersistentTags(shipId, groupIndex, loadoutIndex))
        }
    }
    return tags.toList()
}

fun saveTags(ship: ShipAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = agcStableShipId(ship)
        if (shipId.isNotBlank()) {
            persistTags(shipId, groupIndex, loadoutIndex, tags)
        }
    }
    saveTagsInShip(ship, groupIndex, tags, loadoutIndex)
}

fun persistTags(shipId: String, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
    if (shipId == "") return
    val canonicalTags = canonicalizeWeaponTagNames(tags)
    val storage = Settings.tagStorage.getOrNull(loadoutIndex) ?: return
    val tagsByGroup = storage.modesByShip
        .getOrPut(shipId) { mutableMapOf() }
    tagsByGroup[groupIndex] = canonicalTags.toSet().toList()
}

fun saveTagsInShip(ship: ShipAPI, groupIndex: Int, tags: List<String>, storageIndex: Int) {
    if (!ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)) {
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY, InShipTagStorage())
    }
    (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(storageIndex)
        ?.set(groupIndex, canonicalizeWeaponTagNames(tags))
}

fun loadPersistentTags(shipId: String, groupIndex: Int, loadoutIndex: Int): List<String> {
    if(shipId == "") return emptyList()
    val tags = Settings.tagStorage.getOrNull(loadoutIndex)?.modesByShip?.get(shipId)?.get(groupIndex)
    return canonicalizeWeaponTagNames(tags ?: emptyList())
}

fun loadTagsFromShip(ship: ShipAPI, groupIndex: Int, storageIndex: Int): List<String> {
    return canonicalizeWeaponTagNames((ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.get(
        storageIndex
    )?.get(groupIndex) ?: emptyList())
}

fun doesShipHaveLocalTags(ship: ShipAPI, storageIndex: Int): Boolean {
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipTagStorage)?.tagsByIndex?.containsKey(
        storageIndex
    ) ?: false
}

fun doesShipHaveLocalShipModes(ship: ShipAPI, storageIndex: Int): Boolean {
    return ship.customData.containsKey(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY)
            && (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)?.modes?.containsKey(
        storageIndex
    ) ?: false
}
