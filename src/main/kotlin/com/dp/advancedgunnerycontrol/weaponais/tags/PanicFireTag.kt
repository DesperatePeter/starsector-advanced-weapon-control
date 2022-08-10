package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.utils.getWeaponGroupIndex
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class PanicFireTag(weapon: WeaponAPI, private val threshold: Float) : WeaponAITagBase(weapon) {
    private val groupIndex = getWeaponGroupIndex(weapon)

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 1.0f

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun forceFire(entity: CombatEntityAPI?, predictedLocation: Vector2f?): Boolean {
        return weapon.ship.hullLevel < threshold
    }

    override val advanceWhenTurnedOff: Boolean = true

    override fun advance() {
        if(weapon.ship.hullLevel < threshold){
            forceAutofire(weapon.ship, groupIndex)
        }
    }
}