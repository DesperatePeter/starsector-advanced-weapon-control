package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.ammoLevel
import com.dp.advancedgunnerycontrol.weaponais.isOpportuneTarget
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ConserveAmmoTag(
    weapon: WeaponAPI,
    private val ammoThresholdOverride: Float? = null,
    private val kineticThresholdOverride: Float? = null,
    private val highExplosiveThresholdOverride: Float? = null,
    private val triggerHappinessModifierOverride: Float? = null,
) : WeaponAITagBase(weapon) {
    private fun ammoThreshold(): Float = ammoThresholdOverride ?: Settings.conserveAmmo()
    private fun kineticThreshold(): Float = kineticThresholdOverride ?: Settings.opportunistKineticThreshold()
    private fun highExplosiveThreshold(): Float = highExplosiveThresholdOverride ?: Settings.opportunistHEThreshold()
    private fun triggerHappinessModifier(): Float = triggerHappinessModifierOverride ?: Settings.opportunistModifier()

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return ammoLevel(weapon) > ammoThreshold()
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ammoLevel(weapon) < ammoThreshold()) {
            return isOpportuneTarget(
                solution,
                weapon,
                kineticThreshold(),
                highExplosiveThreshold(),
                triggerHappinessModifier(),
            )
        }
        return true
    }

    override fun isValidSynchronizedTarget(solution: FiringSolution): Boolean = isValidTarget(solution.target)

    override fun shouldFireDuringSynchronizedRelease(solution: FiringSolution): Boolean = isValidTarget(solution.target)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean = weapon.usesAmmo()
}
