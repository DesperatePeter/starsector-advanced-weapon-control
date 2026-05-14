package com.dp.advancedgunnerycontrol.shipdata

import com.dp.advancedgunnerycontrol.gui.refitscreen.ModuleIdManager
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.campaign.fleet.FleetMember
import java.util.WeakHashMap

fun agcStableShipId(ship: ShipAPI): String {
    return generateUniversalFleetMemberId(ship)
}

fun agcStableShipId(ship: FleetMemberAPI): String {
    return generateUniversalFleetMemberId(ship).ifBlank { ship.id.orEmpty() }
}

fun agcShortShipId(rawId: String?, fallback: String = "UNKNOWN"): String {
    return rawId
        .orEmpty()
        .filter { it.isLetterOrDigit() }
        .take(6)
        .uppercase()
        .ifBlank { fallback }
}

fun agcShortShipId(ship: FleetMemberAPI?, fallback: String = "UNKNOWN"): String {
    return ship?.let { agcShortShipId(agcStableShipId(it), fallback) } ?: fallback
}

fun agcShortShipId(ship: ShipAPI?, fallback: String = "UNKNOWN"): String {
    return ship?.let { agcShortShipId(agcStableShipId(it), fallback) } ?: fallback
}

fun generateUniversalFleetMemberId(parentId: String, moduleIndex: Int): String{
    if (moduleIndex < 0) return ""
    return parentId + moduleIndex.toString()
}

private val fleetMemberUniversalIdCache = WeakHashMap<FleetMemberAPI, String>()
private val fleetMemberUniversalIdFallbackLogged = WeakHashMap<FleetMemberAPI, Boolean>()

/**
 * generate unique & persistent fleetMemberId
 * return fleetMemberId-equivalent for modules of big ships
 * return fleetMemberId for regular ships
 * return empty string if something goes wrong
 */
fun generateUniversalFleetMemberId(ship: ShipAPI): String {
    (ModuleIdManager.getUniversalIdIfApplicable(ship))?.let {
        return it
    }
    if (!ship.isStationModule) return ship.fleetMemberId ?: ""
    val parentShip = ship.parentStation ?: return ""
    val parentId = parentShip.fleetMemberId ?: ""
    val index = parentShip.childModulesCopy?.indexOf(ship) ?: -1
    return generateUniversalFleetMemberId(parentId, index)
}

fun generateUniversalFleetMemberId(ship: FleetMemberAPI): String{
    fleetMemberUniversalIdCache[ship]?.let { return it }
    // Instantiating a combat ship is the only reliable path for module-aware ids,
    // but Starsector can require combat-manager context that is not always available.
    val moduleAwareId = runCatching {
        ((ship as? FleetMember)?.instantiateForCombat(null, 0, null) as? ShipAPI)?.let {
            generateUniversalFleetMemberId(it)
        }
    }.onFailure { ex ->
        if (fleetMemberUniversalIdFallbackLogged[ship] != true) {
            Global.getLogger(ShipIdentity::class.java)
                .warn("[AGC_SHIP_ID] Falling back to fleet member id for ${ship.id}", ex)
            fleetMemberUniversalIdFallbackLogged[ship] = true
        }
    }.getOrNull()
    val id = moduleAwareId?.takeIf { it.isNotBlank() } ?: ship.id.orEmpty()
    fleetMemberUniversalIdCache[ship] = id
    return id
}

private object ShipIdentity

fun getWeaponGroupIndex(weapon: WeaponAPI): Int {
    return weapon.ship.weaponGroupsCopy.indexOf(weapon.ship.getWeaponGroupFor(weapon))
}
