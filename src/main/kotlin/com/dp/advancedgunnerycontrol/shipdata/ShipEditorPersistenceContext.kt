package com.dp.advancedgunnerycontrol.shipdata

import com.dp.advancedgunnerycontrol.shipmodes.assignShipModes
import com.dp.advancedgunnerycontrol.shipmodes.canonicalizeShipModeNames
import com.dp.advancedgunnerycontrol.shipmodes.loadPersistedShipModes
import com.dp.advancedgunnerycontrol.shipmodes.loadShipModes
import com.dp.advancedgunnerycontrol.shipmodes.persistShipModes
import com.dp.advancedgunnerycontrol.shipmodes.saveShipModes
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

/**
 * Shared persistence gateway for campaign and direct refit/combat editors.
 * Runtime ships need local custom data plus immediate AI application; campaign
 * fleet members only update persistent storage until the ship is later loaded.
 */
class ShipEditorPersistenceContext(
    val member: FleetMemberAPI,
    val runtimeShip: ShipAPI? = null,
) {
    private data class WeaponTagCacheKey(val groupIndex: Int, val loadoutIndex: Int)

    private val weaponTagsByKey = mutableMapOf<WeaponTagCacheKey, List<String>>()
    private val modesByLoadout = mutableMapOf<Int, List<String>>()

    val shipId: String by lazy {
        runtimeShip?.let { agcStableShipId(it) } ?: agcStableShipId(member)
    }

    fun loadWeaponTags(groupIndex: Int, loadoutIndex: Int): List<String> {
        val key = WeaponTagCacheKey(groupIndex, loadoutIndex)
        return weaponTagsByKey.getOrPut(key) {
            runtimeShip?.let { loadTags(it, groupIndex, loadoutIndex) }
                ?: loadPersistentTags(shipId, groupIndex, loadoutIndex)
        }
    }

    fun saveWeaponTags(groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
        val canonicalTags = canonicalizeWeaponTagNames(tags)
        if (runtimeShip != null) {
            saveTags(runtimeShip, groupIndex, loadoutIndex, canonicalTags)
            applyTagsToWeaponGroup(runtimeShip, groupIndex, canonicalTags)
        } else {
            persistTags(shipId, groupIndex, loadoutIndex, canonicalTags)
        }
        weaponTagsByKey[WeaponTagCacheKey(groupIndex, loadoutIndex)] = canonicalTags
    }

    fun loadModes(loadoutIndex: Int): List<String> {
        return modesByLoadout.getOrPut(loadoutIndex) {
            runtimeShip?.let { loadShipModes(it, loadoutIndex) }
                ?: loadPersistedShipModes(shipId, loadoutIndex)
        }
    }

    fun saveModes(loadoutIndex: Int, modes: List<String>) {
        val canonicalModes = canonicalizeShipModeNames(modes)
        if (runtimeShip != null) {
            saveShipModes(runtimeShip, loadoutIndex, canonicalModes)
            assignShipModes(canonicalModes, runtimeShip)
        } else {
            persistShipModes(shipId, loadoutIndex, canonicalModes)
        }
        modesByLoadout[loadoutIndex] = canonicalModes
    }

    fun loadAllUsedTags(): List<String> = loadAllTags(member, shipId)
}
