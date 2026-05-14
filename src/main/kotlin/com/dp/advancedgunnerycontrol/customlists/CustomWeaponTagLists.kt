package com.dp.advancedgunnerycontrol.customlists

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.persistence.agcPersistentDataOrNull
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.fs.starfarer.api.Global

/**
 * Campaign-persistent custom weapon-tag lists.
 *
 * "All Ships" uses GLOBAL_CUSTOM_LIST_KEY; "This Ship" uses the stable ship id.
 * Empty custom lists are intentional and must not fall back to Classic tags.
 * currentListVersion() exists only to invalidate render caches after list changes.
 */
object CustomWeaponTagListStore {
    const val SCHEMA_VERSION = 1
    private const val GLOBAL_CUSTOM_LIST_KEY = "__all_ships__"

    private var listVersion: Int = 0

    fun currentListVersion(): Int = listVersion

    private fun markListChanged() {
        listVersion++
    }

    private fun defaultCustomTags(): MutableList<String> = CustomWeaponTagListSupport.defaultCustomTags()

    private fun allStates(): MutableMap<String, CustomWeaponTagListState> {
        val persistentData = agcPersistentDataOrNull("custom weapon-tag lists") ?: return mutableMapOf()
        val existing = persistentData[Values.CUSTOM_WEAPON_TAG_LISTS_CAMPAIGN_KEY]
            .validCustomWeaponTagStatesOrNull()
        if (existing != null) return existing
        if (persistentData.containsKey(Values.CUSTOM_WEAPON_TAG_LISTS_CAMPAIGN_KEY)) {
            Global.getLogger(CustomWeaponTagListStore::class.java)
                .warn("Ignoring malformed custom weapon-tag list storage. Creating an empty list store.")
        }

        val created = mutableMapOf<String, CustomWeaponTagListState>()
        persistentData[Values.CUSTOM_WEAPON_TAG_LISTS_CAMPAIGN_KEY] = created
        return created
    }

    private fun existingStatesOrNull(): MutableMap<String, CustomWeaponTagListState>? {
        return agcPersistentDataOrNull("custom weapon-tag lists read")
            ?.get(Values.CUSTOM_WEAPON_TAG_LISTS_CAMPAIGN_KEY)
            .validCustomWeaponTagStatesOrNull()
    }

    fun hasState(shipId: String): Boolean = existingStatesOrNull()?.containsKey(shipId) == true

    fun getStateOrNull(shipId: String): CustomWeaponTagListState? =
        existingStatesOrNull()?.get(shipId)?.also(::migrateAndMarkIfNeeded)

    // Creates campaign-persistent state when absent. Use getStateOrNull for read-only paths.
    fun getOrCreateState(shipId: String): CustomWeaponTagListState {
        return allStates().getOrPut(shipId) {
            CustomWeaponTagListState(
                schemaVersion = SCHEMA_VERSION,
                activeMode = WeaponTagListMode.CUSTOM_GLOBAL.storageId,
                customTags = defaultCustomTags()
            )
        }.also(::migrateAndMarkIfNeeded)
    }

    fun getActiveMode(shipId: String): WeaponTagListMode = getOrCreateState(shipId).activeModeValue()

    fun getActiveModeOrDefault(shipId: String): WeaponTagListMode =
        getStateOrNull(shipId)?.activeModeValue() ?: WeaponTagListMode.CUSTOM_GLOBAL

    fun setActiveMode(shipId: String, mode: WeaponTagListMode) {
        val state = getOrCreateState(shipId)
        if (state.activeMode == mode.storageId) return
        state.activeMode = mode.storageId
        markListChanged()
    }

    fun getSupportedCustomTags(shipId: String): List<String> = supportedCustomTagsForDisplay(customListStateForActiveMode(shipId))

    fun getGlobalSupportedCustomTags(): List<String> = supportedCustomTagsForDisplay(getOrCreateState(GLOBAL_CUSTOM_LIST_KEY))

    fun getStoredCustomTags(shipId: String): List<String> = sortCustomTags(customListStateForActiveMode(shipId).customTags)

    fun setCustomTags(shipId: String, tags: List<String>) {
        val state = customListStateForActiveMode(shipId)
        val sorted = sortCustomTags(tags).toMutableList()
        if (state.customTags == sorted) return
        state.customTags = sorted
        markListChanged()
    }

    fun addCustomTags(shipId: String, tags: List<String>): List<String> {
        return addCustomTagsToState(customListStateForActiveMode(shipId), tags)
    }

    fun removeCustomTags(shipId: String, tags: List<String>): List<String> {
        val removals = canonicalizeWeaponTagNames(tags).toSet()
        val state = customListStateForActiveMode(shipId)
        val remaining = sortCustomTags(state.customTags).filterNot { it in removals }
        if (state.customTags == remaining) return supportedCustomTagsForDisplay(state)
        state.customTags = remaining.toMutableList()
        markListChanged()
        return supportedCustomTagsForDisplay(state)
    }

    fun resetCustomTagsToClassic(shipId: String): List<String> {
        val reset = defaultCustomTags()
        val state = customListStateForActiveMode(shipId)
        if (state.customTags == reset) return reset
        state.customTags = reset
        markListChanged()
        return reset
    }

    fun effectiveTagsForShip(shipId: String): List<String> {
        return when (getActiveMode(shipId)) {
            WeaponTagListMode.CUSTOM,
            WeaponTagListMode.CUSTOM_GLOBAL -> getSupportedCustomTags(shipId)
            else -> Settings.getWeaponTagListForMode(getActiveMode(shipId))
        }
    }

    fun effectiveTagsForShipOrDefault(shipId: String, defaultTags: List<String>): List<String> {
        val state = getStateOrNull(shipId) ?: return defaultTags
        return when (state.activeModeValue()) {
            WeaponTagListMode.CUSTOM -> supportedCustomTagsForDisplay(state)
            WeaponTagListMode.CUSTOM_GLOBAL -> getStateOrNull(GLOBAL_CUSTOM_LIST_KEY)?.let(::supportedCustomTagsForDisplay) ?: defaultTags
            else -> Settings.getWeaponTagListForMode(state.activeModeValue())
        }
    }

    fun importValidTagsIntoCustomList(
        shipId: String,
        tags: List<String>,
        switchToCustom: Boolean = true
    ): CustomWeaponTagImportResult {
        val requestedTags = canonicalizeWeaponTagNames(tags)
        val supportedTags = requestedTags.filter(::isSupportedTag).distinct()
        val unsupportedTags = requestedTags.filterNot(::isSupportedTag).distinct()
        if (supportedTags.isEmpty()) {
            val mode = getActiveModeOrDefault(shipId)
            return CustomWeaponTagImportResult(
                requestedTags = requestedTags,
                importedTags = emptyList(),
                unsupportedTags = unsupportedTags,
                activeModeBefore = mode,
                activeModeAfter = mode,
                customTagsAfter = customListStateForModeOrNull(shipId, mode)
                    ?.let(::supportedCustomTagsForDisplay)
                    .orEmpty()
            )
        }
        val modeBefore = getActiveModeOrDefault(shipId)
        val destinationMode = if (switchToCustom) WeaponTagListMode.CUSTOM else modeBefore
        if (switchToCustom && modeBefore != WeaponTagListMode.CUSTOM) {
            setActiveMode(shipId, WeaponTagListMode.CUSTOM)
        }
        // Preset imports that switch to "This Ship" must write to the same
        // per-ship list that will be visible after the mode switch.
        val customTagsAfter = addCustomTagsToState(
            customListStateForMode(shipId, destinationMode),
            supportedTags
        )
        return CustomWeaponTagImportResult(
            requestedTags = requestedTags,
            importedTags = supportedTags,
            unsupportedTags = unsupportedTags,
            activeModeBefore = modeBefore,
            activeModeAfter = getActiveMode(shipId),
            customTagsAfter = customTagsAfter
        )
    }

    private fun customListStateForActiveMode(shipId: String): CustomWeaponTagListState {
        val shipState = getOrCreateState(shipId)
        return when (shipState.activeModeValue()) {
            WeaponTagListMode.CUSTOM_GLOBAL -> getOrCreateState(GLOBAL_CUSTOM_LIST_KEY)
            else -> shipState
        }
    }

    private fun customListStateForMode(shipId: String, mode: WeaponTagListMode): CustomWeaponTagListState {
        return when (mode) {
            WeaponTagListMode.CUSTOM_GLOBAL -> getOrCreateState(GLOBAL_CUSTOM_LIST_KEY)
            else -> getOrCreateState(shipId)
        }
    }

    private fun customListStateForModeOrNull(shipId: String, mode: WeaponTagListMode): CustomWeaponTagListState? {
        return when (mode) {
            WeaponTagListMode.CUSTOM_GLOBAL -> getStateOrNull(GLOBAL_CUSTOM_LIST_KEY)
            else -> getStateOrNull(shipId)
        }
    }

    private fun addCustomTagsToState(state: CustomWeaponTagListState, tags: List<String>): List<String> {
        val combined = sortCustomTags(state.customTags + tags)
        if (state.customTags == combined) return supportedCustomTagsForDisplay(state)
        state.customTags = combined.toMutableList()
        markListChanged()
        return supportedCustomTagsForDisplay(state)
    }

    fun isSupportedTag(tag: String): Boolean {
        return CustomWeaponTagListSupport.isSupportedTag(tag)
    }

    private fun migrateAndMarkIfNeeded(state: CustomWeaponTagListState) {
        if (migrateIfNeeded(state)) markListChanged()
    }

    private fun migrateIfNeeded(state: CustomWeaponTagListState): Boolean {
        var changed = false
        val sortedTags = sortCustomTags(state.customTags).toMutableList()
        if (state.customTags != sortedTags) {
            state.customTags = sortedTags
            changed = true
        }
        val normalizedMode = WeaponTagListMode.fromStorageId(state.activeMode).storageId
        if (state.activeMode != normalizedMode) {
            state.activeMode = normalizedMode
            changed = true
        }
        if (state.schemaVersion != SCHEMA_VERSION) {
            state.schemaVersion = SCHEMA_VERSION
            changed = true
        }
        return changed
    }

    private fun supportedCustomTagsForDisplay(state: CustomWeaponTagListState): List<String> {
        return CustomWeaponTagListSupport.supportedCustomTagsForDisplay(state)
    }

    private fun sortCustomTags(tags: List<String>): List<String> {
        return CustomWeaponTagListSupport.sortCustomTags(tags)
    }
}
