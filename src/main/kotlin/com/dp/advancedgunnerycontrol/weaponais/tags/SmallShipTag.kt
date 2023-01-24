package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.bigness
import com.dp.advancedgunnerycontrol.weaponais.isBig
import com.dp.advancedgunnerycontrol.weaponais.isSmall
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class SmallShipTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.let { isSmall(it) } ?: false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        if(entity !is ShipAPI) return false
        return entity.isFrigate || entity.isFighter
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val tgtShip = (solution.targetEntity as? ShipAPI) ?: return 10000f
        if(!isSmall(tgtShip)) return 10000f
        return bigness(tgtShip)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        val tgtShip = (solution.targetEntity as? ShipAPI) ?: return false
        return isSmall(tgtShip)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}