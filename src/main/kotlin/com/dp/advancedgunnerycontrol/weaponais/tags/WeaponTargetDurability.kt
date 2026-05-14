package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import kotlin.math.max

private const val EPSILON = 0.0001f

fun estimateFighterEffectiveDurabilityForWeapon(weapon: WeaponAPI, fighter: ShipAPI): Float {
    val hull = fighter.hitpoints.coerceAtLeast(0f)
    val armorBase = (fighter.armorGrid?.armorRating ?: 0f).coerceAtLeast(0f)
    val shieldBase = estimateRemainingShieldBuffer(fighter)

    // Bad-matchup penalty only: ineffective matchups can increase required damage,
    // but effective matchups never reduce durability below baseline.
    val armorPenalty = when (weapon.damageType) {
        DamageType.KINETIC -> 2f
        DamageType.FRAGMENTATION -> 4f
        else -> 1f
    }
    val shieldPenalty = when (weapon.damageType) {
        DamageType.HIGH_EXPLOSIVE -> 2f
        DamageType.FRAGMENTATION -> 4f
        else -> 1f
    }

    return hull + (armorBase * armorPenalty) + (shieldBase * shieldPenalty)
}

fun estimateRemainingShieldBuffer(target: ShipAPI): Float {
    val shield = target.shield ?: return 0f
    val fluxTracker = target.fluxTracker ?: return 0f
    if (shield.type == ShieldAPI.ShieldType.NONE || shield.type == ShieldAPI.ShieldType.PHASE) return 0f
    if (fluxTracker.isOverloadedOrVenting) return 0f

    val shieldDamageTakenMult = target.mutableStats?.shieldDamageTakenMult?.modifiedValue ?: 1f
    val safeShieldDamageTakenMult = max(EPSILON, shieldDamageTakenMult)
    val remainingFluxCapacity = (fluxTracker.maxFlux - fluxTracker.currFlux).coerceAtLeast(0f)
    return remainingFluxCapacity / safeShieldDamageTakenMult
}
