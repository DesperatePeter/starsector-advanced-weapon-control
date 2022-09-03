package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class ShipTargetTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean = entityMatchesShipTarget(entity)

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        if (entityMatchesShipTarget(entity)) return 0.1f
        return 100f
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean = entityMatchesShipTarget(entity)

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun entityMatchesShipTarget(entity: CombatEntityAPI) : Boolean{
        val shipTgt = weapon.ship?.shipTarget
        return  shipTgt == entity
    }
}