package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.combat.meetsFluxCondition
import com.dp.advancedgunnerycontrol.combat.totalFluxBelowThreshold
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.RecentBeamPressureTracker
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class HoldSoftFluxTag(
    weapon: WeaponAPI,
    private val threshold: Float,
    private val totalFluxCap: Float? = null,
    private val recentBeamWindow: Float? = null,
) : WeaponAITagBase(weapon) {
    private val activeCondition = FluxCondition(
        metric = FluxMetric.SOFT,
        comparator = FluxComparator.LESS_THAN,
        threshold = threshold,
        requireTotalFluxBelowSoftFluxCap = true,
        totalFluxCap = totalFluxCap,
    )

    override fun isValidTarget(entity: CombatEntityAPI): Boolean = true

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean =
        weapon.ship?.meetsHoldSoftFluxCondition() ?: true

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1.0f

    override fun shouldFire(solution: FiringSolution): Boolean =
        weapon.ship?.meetsHoldSoftFluxCondition() ?: true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false

    private fun ShipAPI.meetsHoldSoftFluxCondition(): Boolean {
        val beamWindow = recentBeamWindow ?: return meetsFluxCondition(activeCondition)
        if (!totalFluxBelowThreshold(totalFluxCap ?: Settings.softFluxTotalFluxCap())) {
            return false
        }
        if (RecentBeamPressureTracker.wasRecentlyHitByEnemyBeam(this, beamWindow)) {
            return true
        }
        return meetsFluxCondition(activeCondition)
    }
}
