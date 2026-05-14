package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class IgnoreMinorPDTag(weapon: WeaponAPI, private val healthThreshold: Float = 145f) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return when (entity) {
            is ShipAPI -> {
                if (!entity.isFighter) {
                    true
                } else {
                    estimateFighterEffectiveDurabilityForWeapon(weapon, entity) > healthThreshold
                }
            }

            is MissileAPI -> entity.hitpoints > healthThreshold

            else -> false
        }
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = isValidTarget(entity)

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}
