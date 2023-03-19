package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.utils.getWeaponGroupIndex
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.WeaponAPI

class PanicFireTag(weapon: WeaponAPI, private val threshold: Float) : WeaponAITagBase(weapon) {
    private val groupIndex = getWeaponGroupIndex(weapon)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1.0f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun forceFire(solution: FiringSolution?, baseDecision: Boolean): Boolean {
        return weapon.ship.hullLevel < threshold && solution != null
    }

    override val advanceWhenTurnedOff: Boolean = true

    override fun advance() {
        if (weapon.ship.hullLevel < threshold) {
            forceAutofire(weapon.ship, groupIndex)
        }
    }
}