package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.hasPhaseCloak
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class TargetPhaseTag(
    weapon: WeaponAPI,
    private val damageTypeExclusions: DamageTypeExclusions = DamageTypeExclusions.NONE,
) : WeaponAITagBase(weapon) {
    private fun ignoredForWeapon(): Boolean = tagIgnoresThisWeapon(damageTypeExclusions)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (ignoredForWeapon()) return 1f
        return if(isPhaseShip(solution.target)) 0.02f else 1f
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = ignoredForWeapon() || isPhaseShip(entity)

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = !ignoredForWeapon()

    override fun avoidDebris(): Boolean = false

    private fun isPhaseShip(entity: CombatEntityAPI): Boolean = (entity as? ShipAPI)?.hasPhaseCloak() == true
}
