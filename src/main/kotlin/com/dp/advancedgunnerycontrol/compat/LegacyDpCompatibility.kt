package com.dp.advancedgunnerycontrol.compat

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.io.readJsonMapFromFile
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

const val LEGACY_DP_WEAPON_COMP_GLOBAL_TAGS_JSON_FILE_NAME = "AGC_weaponCompGlobalTags.json"

fun loadLegacyDpGlobalWeaponPreset(
    member: FleetMemberAPI,
    groupIndex: Int,
    runtimeShip: ShipAPI? = null,
    primaryFile: String = Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME,
): List<String>? {
    val keys = legacyDpWeaponCompositionKeys(member, groupIndex, runtimeShip)
    if (keys.isEmpty()) return null
    val files = if (primaryFile == Values.WEAPON_COMP_TAG_PRESETS_JSON_FILE_NAME) {
        listOf(LEGACY_DP_WEAPON_COMP_GLOBAL_TAGS_JSON_FILE_NAME)
    } else {
        listOf(primaryFile, LEGACY_DP_WEAPON_COMP_GLOBAL_TAGS_JSON_FILE_NAME).distinct()
    }
    files.forEach { file ->
        val data = readJsonMapFromFile(file)
        keys.forEach { key ->
            if (data.containsKey(key)) return canonicalizeWeaponTagNames(data[key].orEmpty())
        }
    }
    return null
}

private fun legacyDpWeaponCompositionKeys(
    member: FleetMemberAPI,
    groupIndex: Int,
    runtimeShip: ShipAPI?,
): List<String> {
    val keys = mutableListOf<String>()
    legacyDpWeaponCompositionKey(member, groupIndex)?.let(keys::add)
    legacyDpWeaponCompositionKey(runtimeShip, groupIndex)?.let(keys::add)
    return keys.distinct()
}

private fun legacyDpWeaponCompositionKey(member: FleetMemberAPI, groupIndex: Int): String? {
    val variant = member.variant ?: return null
    val group = variant.weaponGroups?.getOrNull(groupIndex) ?: return null
    val names = mutableListOf<String>()
    group.slots.forEach { slot ->
        val weaponId = variant.getWeaponId(slot) ?: return@forEach
        weaponNameOrNull(weaponId)?.let(names::add)
    }
    return legacyDpWeaponNameKey(names)
}

private fun legacyDpWeaponCompositionKey(ship: ShipAPI?, groupIndex: Int): String? {
    val group = ship?.weaponGroupsCopy?.getOrNull(groupIndex) ?: return null
    val names = mutableListOf<String>()
    group.weaponsCopy.forEach { weapon ->
        weapon?.displayName?.takeIf { it.isNotBlank() }?.let(names::add)
    }
    return legacyDpWeaponNameKey(names)
}

private fun legacyDpWeaponNameKey(names: List<String>): String? {
    if (names.isEmpty()) return null
    return names.toSet().sorted().joinToString(prefix = "[", postfix = "]")
}

private fun weaponNameOrNull(weaponId: String): String? =
    runCatching { Global.getSettings().getWeaponSpec(weaponId).weaponName }
        .getOrNull()
        ?.takeIf { it.isNotBlank() }
