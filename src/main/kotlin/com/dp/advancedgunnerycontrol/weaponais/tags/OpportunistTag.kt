package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class OpportunistTag(
    weapon: WeaponAPI,
    private val kineticThresholdOverride: Float? = null,
    private val highExplosiveThresholdOverride: Float? = null,
    private val triggerHappinessModifierOverride: Float? = null,
) : WeaponAITagBase(weapon) {
    private fun kineticThreshold(): Float = kineticThresholdOverride ?: Settings.opportunistKineticThreshold()
    private fun highExplosiveThreshold(): Float = highExplosiveThresholdOverride ?: Settings.opportunistHEThreshold()
    private fun triggerHappinessModifier(): Float = triggerHappinessModifierOverride ?: Settings.opportunistModifier()

    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.isFighter == false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = false

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if (isOpportuneTarget(solution, weapon, kineticThreshold(), highExplosiveThreshold(), triggerHappinessModifier())) {
            1f
        } else {
            10000.0f
        }
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (isAimable(weapon) &&
            !determineIfShotWillHit(solution.aimPoint, effectiveCollRadius(solution.target), weapon)
        ) return false

        return isOpportuneTarget(solution, weapon, kineticThreshold(), highExplosiveThreshold(), triggerHappinessModifier())
    }

    override fun isValidSynchronizedTarget(solution: FiringSolution): Boolean = isValidTarget(solution.target)

    override fun shouldFireDuringSynchronizedRelease(solution: FiringSolution): Boolean {
        return isValidTarget(solution.target) &&
                (!isAimable(weapon) || determineIfShotWillHit(solution.aimPoint, effectiveCollRadius(solution.target), weapon))
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = true
}
