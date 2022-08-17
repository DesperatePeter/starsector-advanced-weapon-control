package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.utils.getWeaponGroupIndex
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class ForceAutofireTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    private val groupIndex = getWeaponGroupIndex(weapon)

    override fun isValidTarget(entity: CombatEntityAPI): Boolean = true

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 1.0f

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    override fun advance() {
        val ship = weapon.ship ?: return
        forceAutofire(ship, groupIndex)
    }

    override val advanceWhenTurnedOff = true
}