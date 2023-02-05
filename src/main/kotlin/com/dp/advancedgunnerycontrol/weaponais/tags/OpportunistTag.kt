package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class OpportunistTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.isFighter == false
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = false

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if (isOpportuneTarget(solution, weapon)) {
            1f
        }else{
            10000.0f
        }
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        if(isAimable(weapon) && !determineIfShotWillHit(solution.aimPoint, effectiveCollRadius(solution.target), weapon)){
            return false
        }
        return isOpportuneTarget(solution, weapon)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = true
}