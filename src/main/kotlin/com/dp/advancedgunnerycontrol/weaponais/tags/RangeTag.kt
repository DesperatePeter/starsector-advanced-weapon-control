package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

class RangeTag(weapon: WeaponAPI, private val threshold: Float) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if(isInRange(solution.aimPoint)) 1.0f else 100f
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        return isInRange(solution.aimPoint)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return isInRange(entity.location)
    }

    private fun isInRange(loc: Vector2f): Boolean{
        return (weapon.location - loc).length() <= threshold * weapon.range
    }
}