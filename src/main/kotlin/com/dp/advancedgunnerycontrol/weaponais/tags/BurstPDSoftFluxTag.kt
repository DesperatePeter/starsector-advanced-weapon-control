package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.fs.starfarer.api.combat.WeaponAPI

class BurstPDSoftFluxTag(weapon: WeaponAPI, freeFireSoftFluxThreshold: Float) : PDTag(
    weapon,
    FluxCondition(
        metric = FluxMetric.SOFT,
        comparator = FluxComparator.GREATER_THAN,
        threshold = freeFireSoftFluxThreshold,
        requireTotalFluxBelowSoftFluxCap = true
    )
)
