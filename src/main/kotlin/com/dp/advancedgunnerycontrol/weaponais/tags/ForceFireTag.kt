package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class ForceFireTag(weapon: WeaponAPI, private val fluxThreshold: Float) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 1f

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    override fun forceFire(entity: CombatEntityAPI?, predictedLocation: Vector2f?, baseDecision: Boolean): Boolean {
        return baseDecision && (weapon.ship?.fluxTracker?.fluxLevel ?: 0.0f) < fluxThreshold
    }
}