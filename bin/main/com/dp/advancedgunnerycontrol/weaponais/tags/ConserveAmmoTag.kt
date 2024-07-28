package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.ammoLevel
import com.dp.advancedgunnerycontrol.weaponais.isOpportuneTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ConserveAmmoTag(weapon: WeaponAPI, private val ammoThreshold: Float) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return ammoLevel(weapon) > ammoThreshold
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ammoLevel(weapon) < ammoThreshold) {
            return isOpportuneTarget(solution, weapon)
        }
        return true
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean = weapon.usesAmmo()
}