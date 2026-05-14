package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.isSmall
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class SmallShipTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.let { isSmall(it) } ?: false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (entity !is ShipAPI) return false
        return isSmall(entity)
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return 1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        val tgtShip = (solution.target as? ShipAPI) ?: return false
        return isSmall(tgtShip)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
