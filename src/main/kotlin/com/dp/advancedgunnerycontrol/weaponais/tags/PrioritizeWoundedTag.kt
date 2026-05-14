package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeWoundedTag(
    weapon: WeaponAPI,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        return solution.target.hullLevel * solution.target.hullLevel
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false
}
