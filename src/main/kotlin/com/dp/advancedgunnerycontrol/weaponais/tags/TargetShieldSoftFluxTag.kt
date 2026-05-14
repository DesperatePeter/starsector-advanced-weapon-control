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

class TargetShieldSoftFluxTag(
    weapon: WeaponAPI,
    private val freeFireSoftFluxThreshold: Float,
    private val shieldThresholdOverride: Float? = null,
    private val totalFluxCap: Float? = null,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun shieldThreshold(): Float = shieldThresholdOverride ?: Settings.targetShieldThreshold()
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    private val restrictionCondition = FluxCondition(
        metric = FluxMetric.SOFT,
        comparator = FluxComparator.GREATER_THAN,
        threshold = freeFireSoftFluxThreshold,
        requireTotalFluxBelowSoftFluxCap = true,
        totalFluxCap = totalFluxCap,
    )

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (ignoredForWeapon()) return true
        return if (weapon.ship?.meetsFluxCondition(restrictionCondition) ?: false) {
            computeShieldFactor(entity, weapon) > shieldThreshold()
        } else {
            true
        }
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        val targetShip = solution.target as? ShipAPI ?: return 1f
        return 1f / (computeShieldFactor(targetShip, weapon) + 0.5f)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ignoredForWeapon()) return true
        return if (weapon.ship?.meetsFluxCondition(restrictionCondition) ?: false) {
            if (solution.target is ShipAPI) {
                if (Settings.ignoreFighterShield() && solution.target.isFighter) {
                    true
                } else {
                    val timeToTravel = computeTimeToTravel(weapon, solution.aimPoint)
                    computeShieldFactor(solution.target, weapon, timeToTravel) > shieldThreshold()
                }
            } else {
                false
            }
        } else {
            true
        }
    }

    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()

    override fun avoidDebris(): Boolean = false
}
