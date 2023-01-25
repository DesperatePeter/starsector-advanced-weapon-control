package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ShipTargetTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean = entityMatchesShipTarget(entity)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (entityMatchesShipTarget(solution.target)) return 0.1f
        return 100f
    }

    override fun shouldFire(solution: FiringSolution): Boolean = entityMatchesShipTarget(solution.target)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun entityMatchesShipTarget(entity: CombatEntityAPI) : Boolean{
        val shipTgt = weapon.ship?.shipTarget
        return  shipTgt == entity
    }
}