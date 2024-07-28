package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.PolarEntityInWeaponCone
import com.dp.advancedgunnerycontrol.weaponais.computeWeaponConeExposureRad
import com.dp.advancedgunnerycontrol.weaponais.getMaxSpreadForNextBurst
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.combat.CombatUtils
import kotlin.math.max

class PrioritizeDense(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val entities = CombatUtils.getEntitiesWithinRange(solution.aimPoint, 500f).filter {
            it.owner != 100 && it.owner != weapon.ship.originalOwner
        }
        val density = entities.map { entity ->
            computeWeaponConeExposureRad(weapon.location, solution.aimPoint, max(10f, weapon.getMaxSpreadForNextBurst()), entity.location, entity.collisionRadius + 100f)
        }.sum() + 0.1f
        return 1f / (density * density)
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}