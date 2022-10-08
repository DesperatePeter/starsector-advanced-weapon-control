package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

// Only fire at full ROF if target is missile or fighter and ammo < ammoThreshold
class ConservePDAmmoTag(weapon: WeaponAPI, private val ammoThreshold: Float) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = (ammoLevel(weapon) >= ammoThreshold)

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return if(ammoLevel(weapon) < ammoThreshold && isValidPDTarget(entity)) 0.01f else 1f
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        if(ammoLevel(weapon) < ammoThreshold)  {
            return isValidPDTarget(entity)
        }
        return true
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean = super.isValid() && weapon.usesAmmo()
}