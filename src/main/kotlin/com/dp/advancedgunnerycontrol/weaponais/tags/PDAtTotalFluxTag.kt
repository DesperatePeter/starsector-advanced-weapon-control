package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.fs.starfarer.api.combat.WeaponAPI

class PDAtTotalFluxTag(weapon: WeaponAPI, threshold: Float) : PDTag(
    weapon,
    FluxCondition(FluxMetric.TOTAL, FluxComparator.GREATER_THAN, threshold)
)
