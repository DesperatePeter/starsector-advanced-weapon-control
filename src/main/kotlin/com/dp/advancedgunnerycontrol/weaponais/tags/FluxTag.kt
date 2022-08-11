package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class FluxTag(weapon: WeaponAPI, private val threshold: Float) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean = true

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return (weapon.ship?.fluxLevel ?: 0f) <= threshold
    }

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 1.0f

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean{
        return (weapon.ship?.fluxLevel ?: 0f) <= threshold
    }

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false
}