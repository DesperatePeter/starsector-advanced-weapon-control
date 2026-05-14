package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.combat.meetsFluxCondition
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.WeaponAPI

class ForceFireSoftFluxTag(
    weapon: WeaponAPI,
    private val softFluxThreshold: Float,
    private val totalFluxCap: Float? = null,
) : WeaponAITagBase(weapon) {
    private val activeCondition = FluxCondition(
        metric = FluxMetric.SOFT,
        comparator = FluxComparator.LESS_THAN,
        threshold = softFluxThreshold,
        requireTotalFluxBelowSoftFluxCap = true,
        totalFluxCap = totalFluxCap,
    )

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    override fun forceFire(solution: FiringSolution?, baseDecision: Boolean): Boolean {
        return baseDecision && (weapon.ship?.meetsFluxCondition(activeCondition) ?: true)
    }
}
