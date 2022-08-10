package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.utils.determineIfShotWillHit
import com.dp.advancedgunnerycontrol.weaponais.isOpportuneTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class OpportunistTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.isFighter == false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = false

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return if (isOpportuneTarget(entity, predictedLocation, weapon)) {
            1f
        }else{
            10000.0f
        }
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {

        if(!determineIfShotWillHit(predictedLocation, entity.collisionRadius.times(0.5f), weapon)){
            return false
        }
        return isOpportuneTarget(entity, predictedLocation, weapon)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = true
}