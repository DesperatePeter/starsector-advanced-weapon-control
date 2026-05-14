package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods

const val bignessFrigate = 0.5f
const val bignessDestroyer = 2f
const val bignessCruiser = 5f
const val bignessCapital = 20f
const val bignessFighter = 0.1f
fun isBig(ship: ShipAPI): Boolean {
    return bigness(ship) > bignessFrigate + 0.1f
}

fun isSmall(ship: ShipAPI): Boolean {
    return bigness(ship) < bignessCruiser - 0.1f
}

fun bigness(ship: ShipAPI): Float {
    return when (ship.hullSize) {
        ShipAPI.HullSize.FRIGATE -> bignessFrigate
        ShipAPI.HullSize.DESTROYER -> bignessDestroyer
        ShipAPI.HullSize.CRUISER -> bignessCruiser
        ShipAPI.HullSize.CAPITAL_SHIP -> bignessCapital
        else -> bignessFighter
    }
}

fun isHostile(entity: CombatEntityAPI): Boolean {
    if((entity as? ShipAPI)?.variant?.hasHullMod(HullMods.VASTBULK) == true) return false
    return entity.owner == 1
}

fun isValidPDTargetForWeapon(target: CombatEntityAPI?, weapon: WeaponAPI): Boolean {
    if (weapon.hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES) && (target as? MissileAPI)?.isFlare == true) {
        return false
    }
    return (target is MissileAPI || ((target as? ShipAPI)?.isFighter == true))
}

fun ShipAPI.determineUniversalShipTarget(): ShipAPI?{
    shipTarget?.let { return it }
    if(this.isStationModule){
        return this.parentStation?.determineUniversalShipTarget()
    }
    (aiFlags?.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI)?.let { return it }
    return null
}