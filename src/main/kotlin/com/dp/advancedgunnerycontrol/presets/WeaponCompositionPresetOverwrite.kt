package com.dp.advancedgunnerycontrol.presets

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Submarkets

internal fun overwriteMatchingGroups(
    sourceMember: FleetMemberAPI,
    sourceGroupIndex: Int,
    loadoutIndexes: List<Int>,
    scope: WeaponPresetScope,
    tags: List<String>,
    runtimeShip: ShipAPI?,
): Int {
    val sourceWeaponKey = getWeaponCompositionPresetKey(sourceMember, sourceGroupIndex)
        ?: return 0
    val sourceShipId = exactSinglePresetShipId(sourceMember)
    var count = overwriteMatchingRuntimeGroups(
        sourceMember,
        runtimeShip,
        sourceWeaponKey,
        sourceShipId,
        loadoutIndexes,
        scope,
        tags
    )
    val ships = overwriteTargetMembers()
        ?: return count
    ships.forEach { target ->
        if (!scope.matchesTarget(sourceMember, sourceShipId, target)) return@forEach
        for (index in 0 until Values.MAX_WEAPON_GROUPS) {
            if (getWeaponCompositionPresetKey(target, index) != sourceWeaponKey) continue
            // Overwrite applies to the normal active tag storage, not just the preset store.
            // Re-sanitize for the target group so cross-ship overwrite cannot persist tags
            // that are disabled for the destination weapon group.
            val targetTags = sanitizeTagsForWeaponGroup(
                member = target,
                groupIndex = index,
                tags = tags,
                importSupportedCustomTags = true
            )
            val context = ShipEditorPersistenceContext(target)
            loadoutIndexes.forEach { loadoutIndex ->
                context.saveWeaponTags(index, loadoutIndex, targetTags)
                count++
            }
        }
    }
    return count
}

fun previewWeaponPresetOverwriteTargetMembers(
    sourceMember: FleetMemberAPI,
    scope: WeaponPresetScope,
): List<FleetMemberAPI> {
    val sourceShipId = exactSinglePresetShipId(sourceMember)
    return overwriteTargetMembers()
        ?.filter { target -> scope.matchesTarget(sourceMember, sourceShipId, target) }
        ?: emptyList()
}

private fun overwriteTargetMembers(): List<FleetMemberAPI>? {
    val sector = Global.getSector() ?: return null
    val membersById = linkedMapOf<String, FleetMemberAPI>()
    fun addMembers(members: List<FleetMemberAPI>?) {
        members
            ?.filterNot { it.isFighterWing }
            ?.forEach { member ->
                val id = member.id ?: return@forEach
                membersById.putIfAbsent(id, member)
            }
    }
    addMembers(sector.playerFleet?.membersWithFightersCopy)
    sector.economy?.marketsCopy
        ?.mapNotNull { market -> market.getSubmarket(Submarkets.SUBMARKET_STORAGE) }
        ?.forEach { storage ->
            addMembers(storage.cargoNullOk?.mothballedShips?.membersListCopy)
        }
    return membersById.values.toList()
}

private fun overwriteMatchingRuntimeGroups(
    sourceMember: FleetMemberAPI,
    runtimeShip: ShipAPI?,
    sourceWeaponKey: String,
    sourceShipId: String,
    loadoutIndexes: List<Int>,
    scope: WeaponPresetScope,
    tags: List<String>,
): Int {
    if (runtimeShip == null || !scope.matchesTarget(sourceMember, sourceShipId, sourceMember)) return 0
    var count = 0
    val context = ShipEditorPersistenceContext(sourceMember, runtimeShip)
    for (index in 0 until Values.MAX_WEAPON_GROUPS) {
        if (getWeaponCompositionPresetKey(sourceMember, index) != sourceWeaponKey) continue
        val targetTags = sanitizeTagsForWeaponGroup(
            member = sourceMember,
            groupIndex = index,
            tags = tags,
            runtimeShip = runtimeShip,
            importSupportedCustomTags = true
        )
        loadoutIndexes.forEach { loadoutIndex ->
            context.saveWeaponTags(index, loadoutIndex, targetTags)
            count++
        }
    }
    return count
}

private fun WeaponPresetScope.matchesTarget(
    sourceMember: FleetMemberAPI,
    sourceShipId: String,
    target: FleetMemberAPI,
): Boolean {
    return when (this) {
        WeaponPresetScope.SINGLE -> target.id == sourceShipId
        WeaponPresetScope.CLASS -> target.hullId == sourceMember.hullId
        WeaponPresetScope.GLOBAL -> true
        WeaponPresetScope.SUGGESTED -> false
    }
}
