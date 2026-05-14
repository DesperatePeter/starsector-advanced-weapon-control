package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.linearDistanceFromWeapon
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeShieldsTag(
    weapon: WeaponAPI,
    private val multiplierOverride: Float? = null,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        return priorityFromPreference(computeShieldFactor(solution.target, weapon), multiplier())
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true
    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()
    override fun avoidDebris(): Boolean = false
}

class PrioritizeHullTag(
    weapon: WeaponAPI,
    private val multiplierOverride: Float? = null,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        return priorityFromPreference(1f - computeShieldFactor(solution.target, weapon), multiplier())
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true
    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()
    override fun avoidDebris(): Boolean = false
}

class PrioritizeCloseTag(
    weapon: WeaponAPI,
    private val multiplierOverride: Float? = null
) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return priorityFromPreference(1f - normalizedTargetDistance(solution, weapon), multiplier())
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true
    override fun isBaseAiOverridable(): Boolean = true
    override fun avoidDebris(): Boolean = false
}

class PrioritizeFarTag(
    weapon: WeaponAPI,
    private val multiplierOverride: Float? = null
) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return priorityFromPreference(normalizedTargetDistance(solution, weapon), multiplier())
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true
    override fun isBaseAiOverridable(): Boolean = true
    override fun avoidDebris(): Boolean = false
}

private fun priorityFromPreference(preference: Float, multiplier: Float): Float {
    val boundedPreference = preference.coerceIn(0f, 1f)
    val boundedMultiplier = multiplier.coerceAtLeast(1f)
    return 1f / (1f + boundedPreference * (boundedMultiplier - 1f))
}

private fun normalizedTargetDistance(solution: FiringSolution, weapon: WeaponAPI): Float {
    val range = weapon.range.coerceAtLeast(1f)
    return (linearDistanceFromWeapon(solution.aimPoint, weapon) / range).coerceIn(0f, 1f)
}
