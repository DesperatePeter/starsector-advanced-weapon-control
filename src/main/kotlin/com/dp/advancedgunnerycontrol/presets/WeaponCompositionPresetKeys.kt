package com.dp.advancedgunnerycontrol.presets

import com.dp.advancedgunnerycontrol.shipdata.getVariantWeaponGroup
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

private const val SUGGESTED_PRESET_LOADOUT_INDEX = -1
internal const val GLOBAL_SCOPE_PREFIX = "global:"

fun getWeaponCompositionPresetKey(member: FleetMemberAPI, groupIndex: Int): String? {
    return getWeaponCompositionPresetWeaponIds(member, groupIndex)?.joinToString(separator = "|")
}

fun getWeaponCompositionPresetKey(ship: ShipAPI, groupIndex: Int): String? {
    return getWeaponCompositionPresetWeaponIds(ship, groupIndex)?.joinToString(separator = "|")
}

fun getWeaponCompositionPresetWeaponNames(member: FleetMemberAPI, groupIndex: Int): List<String> {
    val group = getVariantWeaponGroup(member, groupIndex) ?: return emptyList()
    return group.slots
        .mapNotNull { member.variant?.getWeaponId(it)?.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet()
        .map { weaponId ->
            runCatching { Global.getSettings().getWeaponSpec(weaponId).weaponName }.getOrDefault(weaponId)
        }
}

private fun getWeaponCompositionPresetWeaponIds(member: FleetMemberAPI, groupIndex: Int): List<String>? {
    val group = getVariantWeaponGroup(member, groupIndex) ?: return null
    val weaponIds = group.slots
        .mapNotNull { member.variant?.getWeaponId(it)?.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet()
        .toList()
    return weaponIds.ifEmpty { null }
}

private fun getWeaponCompositionPresetWeaponIds(ship: ShipAPI, groupIndex: Int): List<String>? {
    val group = ship.weaponGroupsCopy?.getOrNull(groupIndex) ?: return null
    val weaponIds = group.weaponsCopy
        .mapNotNull { it?.id?.trim() }
        .filter { it.isNotEmpty() }
        .toSortedSet()
        .toList()
    return weaponIds.ifEmpty { null }
}

internal fun presetLoadoutIndex(scope: WeaponPresetScope, loadoutIndex: Int): Int {
    return if (scope == WeaponPresetScope.SUGGESTED) SUGGESTED_PRESET_LOADOUT_INDEX else loadoutIndex
}

internal fun storageKey(
    member: FleetMemberAPI,
    groupIndex: Int,
    scope: WeaponPresetScope,
    runtimeShip: ShipAPI? = null,
): String? {
    val weaponIds = getWeaponCompositionPresetWeaponIds(member, groupIndex)
        ?: return null
    return when (scope) {
        WeaponPresetScope.SINGLE -> {
            val shipId = exactSinglePresetShipId(member)
            if (shipId.isBlank()) null else "single:$shipId:${weaponIds.joinToString("|")}"
        }
        WeaponPresetScope.CLASS -> "class:${member.hullId}:${weaponIds.joinToString("|")}"
        WeaponPresetScope.GLOBAL -> "$GLOBAL_SCOPE_PREFIX${weaponIds.joinToString("|")}"
        WeaponPresetScope.SUGGESTED -> {
            if (weaponIds.size != 1) null else "suggested:${weaponIds.first()}"
        }
    }
}

internal fun exactSinglePresetShipId(member: FleetMemberAPI): String {
    // Single-scope presets must identify the exact campaign fleet member, not a
    // combat-facing fleetMemberId that can look hull/class-like after temporary
    // ship instantiation.
    return member.id?.trim().orEmpty()
}

internal fun suggestedLoadKeys(
    member: FleetMemberAPI,
    groupIndex: Int,
): List<String>? {
    val weaponIds = getWeaponCompositionPresetWeaponIds(member, groupIndex)
        ?: return null
    return weaponIds.map { "suggested:$it" }
}
