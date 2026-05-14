package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeShipTag(weapon: WeaponAPI, private val multiplierOverride: Float? = null) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if((solution.target as? ShipAPI)?.isFighter == false) 1f / multiplier() else 1f
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = (entity as? ShipAPI)?.isFighter == false

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
