package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.ammoLevel
import com.dp.advancedgunnerycontrol.weaponais.isValidPDTargetForWeapon
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

// Only fire at full ROF if target is missile or fighter and ammo < ammoThreshold
class ConservePDAmmoTag(weapon: WeaponAPI, private val ammoThreshold: Float) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = (ammoLevel(weapon) >= ammoThreshold)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if (ammoLevel(weapon) < ammoThreshold && isValidPDTargetForWeapon(solution.target, weapon)) 0.01f else 1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if (ammoLevel(weapon) < ammoThreshold) {
            return isValidPDTargetForWeapon(solution.target, weapon)
        }
        return true
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean = super.isValid() && weapon.usesAmmo()
}