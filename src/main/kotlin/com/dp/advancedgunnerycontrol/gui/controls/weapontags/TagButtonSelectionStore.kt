package com.dp.advancedgunnerycontrol.gui.controls.weapontags

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object TagButtonSelectionStore {
    fun loadTagsForContext(
        ship: FleetMemberAPI,
        group: Int,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): List<String> {
        return (persistenceContext ?: ShipEditorPersistenceContext(ship, runtimeShip))
            .loadWeaponTags(group, AGCGUI.storageIndex)
    }

    fun saveTagsForContext(
        ship: FleetMemberAPI,
        group: Int,
        runtimeShip: ShipAPI?,
        tags: List<String>,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ) {
        (persistenceContext ?: ShipEditorPersistenceContext(ship, runtimeShip))
            .saveWeaponTags(group, AGCGUI.storageIndex, tags)
    }

    fun repairAndPersistSelectedTags(
        ship: FleetMemberAPI,
        group: Int,
        runtimeShip: ShipAPI? = null,
        visibleTags: List<String> = Settings.getCurrentWeaponTagList(),
        loadedTagsOverride: List<String>? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): MutableList<String> {
        val loaded = loadedTagsOverride ?: loadTagsForContext(ship, group, runtimeShip, persistenceContext)
        val sanitized = selectedTagsAllowedForDisplay(ship, group, visibleTags, loaded)
        val shouldPersist = sanitized != loaded
        if (shouldPersist) {
            saveTagsForContext(ship, group, runtimeShip, sanitized, persistenceContext)
        }
        return sanitized
    }

    fun selectedTagsAllowedForDisplay(
        ship: FleetMemberAPI,
        group: Int,
        visibleTags: List<String> = Settings.getCurrentWeaponTagList(),
        loadedTags: List<String>,
    ): MutableList<String> {
        val visibleTagSet = visibleTags.toSet()
        val sanitized = loadedTags
            .filter { it in visibleTagSet }
            .toMutableList()
        var changed = true
        while (changed) {
            changed = false
            sanitized.toList().forEach { persistedTag ->
                if (TagButtonAvailability.isTagUnavailable(ship, group, persistedTag, sanitized)) {
                    sanitized.remove(persistedTag)
                    changed = true
                }
            }
        }
        return sanitized
    }
}
