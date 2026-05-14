package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.presets.getWeaponCompositionPresetKey
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetCleanBaselineStore {
    private val cleanTagsByKey = mutableMapOf<PresetCleanBaselineKey, List<String>>()

    // This is UI state, not preset data: it preserves the last explicit
    // Save/Load/Reset baseline across closing and reopening the AGC editor.
    fun get(ship: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int): List<String>? {
        val keys = baselineKeys(ship, groupIndex, loadoutIndex)
        if (keys.isEmpty()) return null
        keys.firstNotNullOfOrNull { key -> cleanTagsByKey[key] }?.let { return it }
        val shipIds = keys.map { it.shipId }.toSet()
        val referenceKey = keys.first()
        val matchingWeaponBaselines = cleanTagsByKey
            .filterKeys {
                it.shipId in shipIds &&
                    it.loadoutIndex == referenceKey.loadoutIndex &&
                    it.weaponKey == referenceKey.weaponKey
            }
            .values
            .distinct()
        return matchingWeaponBaselines.singleOrNull()
    }

    fun put(ship: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int, tags: List<String>) {
        baselineKeys(ship, groupIndex, loadoutIndex).forEach { key ->
            cleanTagsByKey[key] = tags
        }
    }

    private fun baselineKeys(ship: FleetMemberAPI, groupIndex: Int, loadoutIndex: Int): List<PresetCleanBaselineKey> {
        val weaponKey = getWeaponCompositionPresetKey(ship, groupIndex) ?: return emptyList()
        return shipKeys(ship).map { shipId ->
            PresetCleanBaselineKey(shipId, loadoutIndex, groupIndex, weaponKey)
        }
    }

    private fun shipKeys(ship: FleetMemberAPI): List<String> {
        return listOf(
            agcStableShipId(ship),
            ship.id.orEmpty(),
        )
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
