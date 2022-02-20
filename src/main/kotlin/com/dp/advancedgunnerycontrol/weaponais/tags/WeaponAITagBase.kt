package com.dp.advancedgunnerycontrol.weaponais.tags

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

abstract class WeaponAITagBase(protected val weapon: WeaponAPI) {
    abstract fun isValidTarget(entity: CombatEntityAPI) : Boolean
    abstract fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f) : Float
    abstract fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f) : Boolean
    abstract fun isBaseAiOverridable() : Boolean
    abstract fun avoidDebris() : Boolean
}