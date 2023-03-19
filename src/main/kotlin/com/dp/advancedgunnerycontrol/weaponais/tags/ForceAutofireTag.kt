package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.utils.getWeaponGroupIndex
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ForceAutofireTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    private val groupIndex = getWeaponGroupIndex(weapon)

    override fun isValidTarget(entity: CombatEntityAPI): Boolean = true

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1.0f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    override fun advance() {
        val ship = weapon.ship ?: return
        forceAutofire(ship, groupIndex)
    }

    override val advanceWhenTurnedOff = true
}