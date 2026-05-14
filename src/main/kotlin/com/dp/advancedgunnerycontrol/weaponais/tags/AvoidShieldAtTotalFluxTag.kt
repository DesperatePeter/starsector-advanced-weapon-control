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

// Allows targeting of anything when flux < fluxThreshold, otherwise avoids shields. Always prioritises by target shield factor
class AvoidShieldAtTotalFluxTag(
    weapon: WeaponAPI,
    private val shieldThresholdOverride: Float? = null,
    private val fluxThresholdOverride: Float? = null
) : WeaponAITagBase(weapon) {
    private fun shieldThreshold(): Float = shieldThresholdOverride ?: Settings.avoidShieldThreshold()
    private fun freeFireCondition(): FluxCondition = FluxCondition(
        FluxMetric.TOTAL,
        FluxComparator.LESS_OR_EQUAL,
        fluxThresholdOverride ?: Settings.avoidShieldAtTotalFlux()
    )

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return if (weapon.ship.meetsFluxCondition(freeFireCondition())) {
            true
        } else {
            computeShieldFactor(entity, weapon) < shieldThreshold()
        }
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return computeShieldFactor(solution.target, weapon) + 0.1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        return if (weapon.ship.meetsFluxCondition(freeFireCondition())) {
            true
        } else if (solution.target is ShipAPI) {
            if (Settings.ignoreFighterShield() && solution.target.isFighter) {
                true
            } else {
                val ttt = computeTimeToTravel(weapon, solution.aimPoint)
                computeShieldFactor(solution.target, weapon, ttt) < shieldThreshold()
            }
        } else {
            false
        }

    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
