package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class OverloadTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.fluxTracker?.isOverloaded ?: false
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if (isValidTarget(solution.target)) 0.1f else 10f
    }

    override fun shouldFire(solution: FiringSolution): Boolean = isValidTarget(solution.target)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

}