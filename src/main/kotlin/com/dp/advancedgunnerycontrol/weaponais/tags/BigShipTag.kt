package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.bigness
import com.dp.advancedgunnerycontrol.weaponais.isBig
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class BigShipTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.let { isBig(it) } ?: false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if (entity !is ShipAPI) return false
        return entity.isCapital || entity.isCruiser
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val tgtShip = (solution.target as? ShipAPI) ?: return 10000f
        if (!isBig(tgtShip)) return 10000f
        return 1f / bigness(tgtShip)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        val tgtShip = (solution.target as? ShipAPI) ?: return false
        return isBig(tgtShip)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}