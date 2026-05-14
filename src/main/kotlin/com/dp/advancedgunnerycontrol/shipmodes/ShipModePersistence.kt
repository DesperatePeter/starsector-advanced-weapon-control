package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.shipdata.doesShipHaveLocalShipModes
import com.dp.advancedgunnerycontrol.utils.InShipShipModeStorage
import com.fs.starfarer.api.combat.ShipAPI

fun persistShipModes(shipId: String, loadoutIndex: Int, tags: List<String>) {
    if (shipId == "") return
    val storage = Settings.shipModeStorage.getOrNull(loadoutIndex) ?: return
    storage.modesByShip[shipId] = mutableMapOf()
    storage.modesByShip[shipId]?.set(0, canonicalizeShipModeNames(tags))
}

private fun migrateLegacyShipModeStorageIfNeeded(ship: ShipAPI): InShipShipModeStorage? {
    val existing = ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage
    if (existing != null) return existing
    val legacy = ship.customData[Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY] as? InShipShipModeStorage ?: return null
    ship.setCustomData(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY, legacy)
    ship.customData.remove(Values.CUSTOM_SHIP_DATA_WEAPONS_TAG_KEY)
    return legacy
}

private fun getLocalShipModeStorage(ship: ShipAPI): InShipShipModeStorage? {
    return (ship.customData[Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY] as? InShipShipModeStorage)
        ?: migrateLegacyShipModeStorageIfNeeded(ship)
}

private fun getOrCreateLocalShipModeStorage(ship: ShipAPI): InShipShipModeStorage {
    return getLocalShipModeStorage(ship) ?: InShipShipModeStorage().also {
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_SHIP_MODES_KEY, it)
    }
}

fun saveShipModesInShip(ship: ShipAPI, tags: List<String>, storageIndex: Int) {
    getOrCreateLocalShipModeStorage(ship).modes[storageIndex] =
        canonicalizeShipModeNames(tags).toMutableList()
}

fun saveShipModes(ship: ShipAPI, loadoutIndex: Int, tags: List<String>) {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = agcStableShipId(ship)
        persistShipModes(shipId, loadoutIndex, tags)
    } else {
        saveShipModesInShip(ship, tags, loadoutIndex)
    }
}

fun loadPersistedShipModes(shipId: String, loadoutIndex: Int): List<String> {
    if (shipId == "") return emptyList()
    return canonicalizeShipModeNames(
        Settings.shipModeStorage.getOrNull(loadoutIndex)?.modesByShip?.get(shipId)?.get(0) ?: emptyList()
    )
}

fun addPersistentShipMode(shipId: String, loadoutIndex: Int, mode: String){
    val modes = loadPersistedShipModes(shipId, loadoutIndex)
    val newModes = canonicalizeShipModeNames(modes + mode)
    persistShipModes(shipId, loadoutIndex, newModes)
}

fun removePersistentShipMode(shipId: String, loadoutIndex: Int, mode: String){
    val canonicalMode = canonicalizeShipModeName(mode)
    val modes = loadPersistedShipModes(shipId, loadoutIndex)
    persistShipModes(shipId, loadoutIndex, modes.filter { canonicalizeShipModeName(it) != canonicalMode })
}

fun loadShipModesFromShip(ship: ShipAPI, storageIndex: Int): List<String> {
    return canonicalizeShipModeNames(
        getLocalShipModeStorage(ship)?.modes?.get(storageIndex) ?: emptyList()
    )
}

fun loadShipModes(ship: ShipAPI, loadoutIndex: Int): List<String> {
    if (Settings.enableCombatChangePersistence()) {
        val shipId = agcStableShipId(ship)
        return loadPersistedShipModes(shipId, loadoutIndex)
    }

    migrateLegacyShipModeStorageIfNeeded(ship)
    if (!doesShipHaveLocalShipModes(ship, loadoutIndex)) {
        val shipId = agcStableShipId(ship)
        return loadPersistedShipModes(shipId, loadoutIndex)
    }

    return loadShipModesFromShip(ship, loadoutIndex)
}