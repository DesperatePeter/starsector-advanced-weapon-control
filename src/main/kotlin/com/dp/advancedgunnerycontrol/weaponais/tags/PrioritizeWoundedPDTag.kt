package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.isValidPDTargetForWeapon
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeWoundedPDTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        if (!isValidPDTargetForWeapon(solution.target, weapon)) return 1f
        val durability = when (val target = solution.target) {
            is MissileAPI -> target.hitpoints.coerceAtLeast(0f)
            is ShipAPI -> estimateFighterEffectiveDurabilityForWeapon(weapon, target)
            else -> return 1f
        }
        return durability / (durability + PRIORITY_SCALE)
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    private companion object {
        const val PRIORITY_SCALE = 1000f
    }
}
