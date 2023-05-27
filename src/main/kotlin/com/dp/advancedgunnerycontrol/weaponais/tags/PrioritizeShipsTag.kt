package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeShipsTag(weapon: WeaponAPI, private val multiplier: Float) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if((solution.target as? ShipAPI)?.isFighter == false) 1f / multiplier else 1f
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = (entity as? ShipAPI)?.isFighter == false

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}