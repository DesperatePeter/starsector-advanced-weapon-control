package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.combat.meetsFluxCondition
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class TargetShieldHardFluxTag(
    weapon: WeaponAPI,
    private val freeFireHardFluxThreshold: Float,
    private val shieldThresholdOverride: Float? = null,
    private val totalFluxCap: Float? = null,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun shieldThreshold(): Float = shieldThresholdOverride ?: Settings.targetShieldThreshold()
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    private val freeFireCondition = FluxCondition(
        FluxMetric.HARD,
        FluxComparator.LESS_OR_EQUAL,
        freeFireHardFluxThreshold,
        totalFluxCap = totalFluxCap,
    )

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (ignoredForWeapon()) return true
        return if (weapon.ship?.meetsFluxCondition(freeFireCondition) ?: true) {
            true
        } else {
            computeShieldFactor(entity, weapon) > shieldThreshold()
        }
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        val targetShip = solution.target as? ShipAPI ?: return 1f
        return 1f / (computeShieldFactor(targetShip, weapon) + 0.5f)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ignoredForWeapon()) return true
        return if (weapon.ship?.meetsFluxCondition(freeFireCondition) ?: true) {
            true
        } else if (solution.target is ShipAPI) {
            if (Settings.ignoreFighterShield() && solution.target.isFighter) {
                true
            } else {
                val timeToTravel = computeTimeToTravel(weapon, solution.aimPoint)
                computeShieldFactor(solution.target, weapon, timeToTravel) > shieldThreshold()
            }
        } else {
            false
        }
    }

    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()

    override fun avoidDebris(): Boolean = false
}
