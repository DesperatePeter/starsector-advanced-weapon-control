package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

// Prioritises missiles > fighters > small ships > big ships
class PrioritisePDTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return (entity as? ShipAPI)?.let { isSmall(it) } ?: false || (isPD(weapon) &&
                isValidPDTargetForWeapon(entity, weapon))
    }

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean = entity is MissileAPI

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return if (isValidPDTargetForWeapon(solution.target, weapon)) {
            0.02f
        } else {
            (solution.target as? ShipAPI)?.let { bigness(it) } ?: 10f
        }
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true
    override fun isBaseAiOverridable(): Boolean = true
    override fun avoidDebris(): Boolean = false
}