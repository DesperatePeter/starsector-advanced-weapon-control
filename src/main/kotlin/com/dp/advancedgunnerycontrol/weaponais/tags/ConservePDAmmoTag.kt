package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

// Only fire at full ROF if target is missile or fighter and ammo < ammoThreshold
class ConservePDAmmoTag(weapon: WeaponAPI, private val ammoThreshold: Float) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = true

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 1f

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        if(ammoLevel(weapon) < ammoThreshold)  {
            return (entity as? ShipAPI)?.isFighter == true || (entity is MissileAPI) && isPD(weapon)
        }
        return true
    }

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    override fun isValid(): Boolean = super.isValid() && weapon.usesAmmo()
}