package com.dp.advancedgunnerycontrol.customlists

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.persistence.agcPersistentDataOrNull
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipmodes.ShipModes
import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeNames
import com.dp.advancedgunnerycontrol.shipmodes.classicShipModeNames
import com.dp.advancedgunnerycontrol.shipmodes.isSupportedShipModeName
import com.dp.advancedgunnerycontrol.shipmodes.parseShipMode
import com.fs.starfarer.api.Global

/**
 * Campaign-persistent custom ship-mode lists.
 *
 * Ship-mode visibility follows CustomWeaponTagListStore's active list mode:
 * "All Ships" resolves through GLOBAL_CUSTOM_LIST_KEY, and "This Ship" resolves
 * through the stable ship id. currentListVersion() exists only to invalidate
 * render caches after list changes.
 */
object CustomShipModeListStore {
    const val SCHEMA_VERSION = 2
    private const val GLOBAL_CUSTOM_LIST_KEY = "__all_ships__"
    private var listVersion: Int = 0

    fun currentListVersion(): Int = listVersion

    private fun markListChanged() {
        listVersion++
    }

    private fun allStates(): MutableMap<String, CustomShipModeListState> {
        val persistentData = agcPersistentDataOrNull("custom ship-mode lists") ?: return mutableMapOf()
        val existing = persistentData[Values.CUSTOM_SHIP_MODE_LISTS_CAMPAIGN_KEY]
            .validCustomShipModeStatesOrNull()
        if (existing != null) return existing
        if (persistentData.containsKey(Values.CUSTOM_SHIP_MODE_LISTS_CAMPAIGN_KEY)) {
            Global.getLogger(CustomShipModeListStore::class.java)
                .warn("Ignoring malformed custom ship-mode list storage. Creating an empty list store.")
        }

        val created = mutableMapOf<String, CustomShipModeListState>()
        persistentData[Values.CUSTOM_SHIP_MODE_LISTS_CAMPAIGN_KEY] = created
        return created
    }

    private fun existingStatesOrNull(): MutableMap<String, CustomShipModeListState>? {
        return agcPersistentDataOrNull("custom ship-mode lists read")
            ?.get(Values.CUSTOM_SHIP_MODE_LISTS_CAMPAIGN_KEY)
            .validCustomShipModeStatesOrNull()
    }

    fun getStateOrNull(shipId: String): CustomShipModeListState? =
        existingStatesOrNull()?.get(shipId)?.also(::migrateAndMarkIfNeeded)

    // Creates campaign-persistent state when absent. Use getStateOrNull for read-only paths.
    fun getOrCreateState(shipId: String, fallbackCustomModes: List<String> = Settings.getCurrentShipModeNames()): CustomShipModeListState {
        val states = allStates()
        val existing = states[shipId]
        if (existing != null) {
            migrateAndMarkIfNeeded(existing)
            return existing
        }
        val created = CustomShipModeListState(
            customModes = defaultCustomModeNames(fallbackCustomModes).toMutableList()
        )
        states[shipId] = created
        markListChanged()
        return created
    }

    fun effectiveModesForShipOrDefault(shipId: String, fallbackCustomModes: List<String>): List<ShipModes> {
        // Legacy enum-only projection. Parameterized runtime paths must use
        // exact names from effectiveModeNamesForShipOrDefault(...), because
        // enum conversion collapses distinct variants such as Vent(...).
        return effectiveModeNamesForShipOrDefault(shipId, fallbackCustomModes)
            .mapNotNull { parseShipMode(it)?.mode }
            .distinct()
    }

    fun effectiveModeNamesForShipOrDefault(shipId: String, fallbackCustomModes: List<String>): List<String> {
        return when (CustomWeaponTagListStore.getActiveModeOrDefault(shipId)) {
            WeaponTagListMode.CUSTOM -> getStateOrNull(shipId)?.let(::visibleCustomModeNames)
                ?: sanitizeModeNames(fallbackCustomModes)
                    .ifEmpty { defaultCustomModeNames(fallbackCustomModes) }
            WeaponTagListMode.CUSTOM_GLOBAL -> getStateOrNull(GLOBAL_CUSTOM_LIST_KEY)?.let(::visibleCustomModeNames)
                ?: sanitizeModeNames(fallbackCustomModes)
                    .ifEmpty { defaultCustomModeNames(fallbackCustomModes) }
            WeaponTagListMode.NOVICE -> Settings.getShipModeNamesForMode(WeaponTagListMode.NOVICE)
            WeaponTagListMode.CLASSIC -> Settings.getShipModeNamesForMode(WeaponTagListMode.CLASSIC)
            WeaponTagListMode.COMPLETE -> Settings.getShipModeNamesForMode(WeaponTagListMode.COMPLETE)
        }
    }

    fun isCustomListModeActive(shipId: String): Boolean =
        CustomWeaponTagListStore.getActiveModeOrDefault(shipId).isCustom

    fun getCustomModeNamesForEditing(shipId: String, fallbackCustomModes: List<String> = Settings.getCurrentShipModeNames()): List<String> {
        return getOrCreateState(customListKeyForActiveMode(shipId), fallbackCustomModes).customModes
            .let(::sanitizeModeNames)
    }

    fun addCustomShipModesToCustomList(shipId: String, modes: List<String>): List<String> {
        val state = getOrCreateState(customListKeyForActiveMode(shipId))
        val current = sanitizeModeNames(state.customModes)
        val updated = sanitizeModeNames(current + modes).toMutableList()
        if (state.customModes == updated) return state.customModes
        state.customModes = updated
        markListChanged()
        return state.customModes
    }

    fun removeCustomShipModesFromCustomList(shipId: String, modes: List<String>): List<String> {
        val state = getOrCreateState(customListKeyForActiveMode(shipId))
        val removals = sanitizeModeNames(modes).toSet()
        val updated = sanitizeModeNames(state.customModes)
            .filterNot { it in removals }
            .toMutableList()
        if (state.customModes == updated) return state.customModes
        state.customModes = updated
        markListChanged()
        return state.customModes
    }

    private fun migrateAndMarkIfNeeded(state: CustomShipModeListState) {
        if (migrateIfNeeded(state)) markListChanged()
    }

    private fun migrateIfNeeded(state: CustomShipModeListState): Boolean {
        var changed = false
        val normalizedMode = WeaponTagListMode.fromStorageId(state.activeMode).storageId
        if (state.activeMode != normalizedMode) {
            state.activeMode = normalizedMode
            changed = true
        }
        val modes = sanitizeModeNames(state.customModes)
        val updatedModes = modes.toMutableList()
        if (state.customModes != updatedModes) {
            state.customModes = updatedModes
            changed = true
        }
        if (state.schemaVersion != SCHEMA_VERSION) {
            state.schemaVersion = SCHEMA_VERSION
            changed = true
        }
        return changed
    }

    private fun visibleCustomModeNames(state: CustomShipModeListState): List<String> =
        sanitizeModeNames(state.customModes)

    private fun customListKeyForActiveMode(shipId: String): String {
        return when (CustomWeaponTagListStore.getActiveModeOrDefault(shipId)) {
            WeaponTagListMode.CUSTOM_GLOBAL -> GLOBAL_CUSTOM_LIST_KEY
            else -> shipId
        }
    }

    private fun defaultCustomModeNames(fallbackCustomModes: List<String> = Settings.getCurrentShipModeNames()): List<String> =
        sanitizeModeNames(
            fallbackCustomModes
                .ifEmpty(::classicShipModeNames)
        )

    private fun sanitizeModeNames(modes: List<String>): List<String> =
        canonicalizeShipModeNames(modes).filter(::isSupportedShipModeName).distinct()
}
