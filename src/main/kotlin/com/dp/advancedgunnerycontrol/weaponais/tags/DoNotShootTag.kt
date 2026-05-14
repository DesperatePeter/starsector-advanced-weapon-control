package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.WeaponAPI

class DoNotShootTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1.0f

    override fun shouldFire(solution: FiringSolution): Boolean = false

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = false
}
