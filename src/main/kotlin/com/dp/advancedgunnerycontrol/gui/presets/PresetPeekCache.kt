package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.presets.WeaponCompositionPresetPeekResult
import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.presets.getWeaponCompositionPresetKey
import com.dp.advancedgunnerycontrol.presets.peekWeaponPreset
import com.fs.starfarer.api.fleet.FleetMemberAPI

class PresetPeekCache {
    companion object {
        fun peek(
            presetPeekCache: PresetPeekCache?,
            member: FleetMemberAPI,
            groupIndex: Int,
            loadoutIndex: Int,
            scope: WeaponPresetScope,
            backend: WeaponPresetBackend,
            file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
        ): WeaponCompositionPresetPeekResult {
            return presetPeekCache?.peek(member, groupIndex, loadoutIndex, scope, backend, file)
                ?: peekWeaponPreset(member, groupIndex, loadoutIndex, scope, backend, file)
        }
    }

    private data class Key(
        val stableShipId: String,
        val rawMemberId: String,
        val hullId: String,
        val groupIndex: Int,
        val loadoutIndex: Int,
        val scope: WeaponPresetScope,
        val backend: WeaponPresetBackend,
        val weaponKey: String,
        val file: String,
    )

    private val resultsByKey = mutableMapOf<Key, WeaponCompositionPresetPeekResult>()

    fun peek(
        member: FleetMemberAPI,
        groupIndex: Int,
        loadoutIndex: Int,
        scope: WeaponPresetScope,
        backend: WeaponPresetBackend,
        file: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
    ): WeaponCompositionPresetPeekResult {
        val key = Key(
            stableShipId = agcStableShipId(member),
            rawMemberId = member.id?.trim().orEmpty(),
            hullId = member.hullId.orEmpty(),
            groupIndex = groupIndex,
            loadoutIndex = loadoutIndex,
            scope = scope,
            backend = backend,
            weaponKey = getWeaponCompositionPresetKey(member, groupIndex).orEmpty(),
            file = file,
        )
        return resultsByKey.getOrPut(key) {
            peekWeaponPreset(member, groupIndex, loadoutIndex, scope, backend, file)
        }
    }

    fun clear() {
        resultsByKey.clear()
    }
}
