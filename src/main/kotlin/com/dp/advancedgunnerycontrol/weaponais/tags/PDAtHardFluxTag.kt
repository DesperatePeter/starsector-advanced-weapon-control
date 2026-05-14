package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxCondition
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.fs.starfarer.api.combat.WeaponAPI

class PDAtHardFluxTag(weapon: WeaponAPI, threshold: Float) : PDTag(
    weapon,
    FluxCondition(FluxMetric.HARD, FluxComparator.GREATER_THAN, threshold)
)
