package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.combat.meetsFluxCondition
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class HoldTotalFluxTag(
    weapon: WeaponAPI,
    private val threshold: Float,
    private val totalFluxCap: Float? = null,
) : WeaponAITagBase(weapon) {
    private val activeCondition = FluxCondition(
        FluxMetric.TOTAL,
        FluxComparator.LESS_OR_EQUAL,
        threshold,
        totalFluxCap = totalFluxCap,
    )

    override fun isValidTarget(entity: CombatEntityAPI): Boolean = true

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return weapon.ship?.meetsFluxCondition(activeCondition) ?: true
    }

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1.0f

    override fun shouldFire(solution: FiringSolution): Boolean {
        return weapon.ship?.meetsFluxCondition(activeCondition) ?: true
    }

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false
}
