package com.dp.advancedgunnerycontrol.weaponais.tags

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

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val tgtShip = (entity as? ShipAPI) ?: return 10000f
        if(!isSmall(tgtShip)) return 10000f
        return bigness(tgtShip)
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        val tgtShip = (entity as? ShipAPI) ?: return false
        return isSmall(tgtShip)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}