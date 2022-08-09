package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.times_
import com.dp.advancedgunnerycontrol.weaponais.vectorFromAngleDeg
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

abstract class WeaponAITagBase(protected val weapon: WeaponAPI) {
    abstract fun isValidTarget(entity: CombatEntityAPI) : Boolean
    abstract fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f) : Float
    abstract fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f) : Boolean
    abstract fun isBaseAiOverridable() : Boolean
    abstract fun avoidDebris() : Boolean
    open fun isValid() : Boolean {
        return !Settings.weaponBlacklist.contains(weapon.id)
    }
    open fun forceFire(entity: CombatEntityAPI?, predictedLocation: Vector2f?) : Boolean = false
}