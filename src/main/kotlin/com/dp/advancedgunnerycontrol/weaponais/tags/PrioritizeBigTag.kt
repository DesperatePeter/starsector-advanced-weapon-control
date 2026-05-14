package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.bigness
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeBigTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val targetShip = solution.target as? ShipAPI ?: return 1f
        return 1f / bigness(targetShip)
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
