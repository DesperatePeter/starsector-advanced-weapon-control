package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class TargetShieldTag(
    weapon: WeaponAPI,
    private val thresholdOverride: Float? = null,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) :
    WeaponAITagBase(weapon) {
    private fun threshold(): Float = thresholdOverride ?: Settings.targetShieldThreshold()
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (ignoredForWeapon()) return true
        return computeShieldFactor(entity, weapon) > threshold()
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        val tgtShip = (solution.target as? ShipAPI) ?: return 1f
        return 1f / (computeShieldFactor(tgtShip, weapon) + 0.5f)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ignoredForWeapon()) return true
        val tgtShip = (solution.target as? ShipAPI) ?: return false
        if (Settings.ignoreFighterShield() && tgtShip.isFighter) {
            return true
        }
        val ttt = computeTimeToTravel(weapon, solution.aimPoint)
        val sFactor = computeShieldFactor(tgtShip, weapon, ttt)
        return sFactor > threshold()
    }

    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()

    override fun avoidDebris(): Boolean = false
}
