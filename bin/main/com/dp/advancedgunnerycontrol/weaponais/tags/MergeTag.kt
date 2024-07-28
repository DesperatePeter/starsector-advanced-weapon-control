package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.combat.WeaponAPI

class MergeTag(weapon: WeaponAPI) : WeaponAITagBase(weapon) {

    public val originalGroup = weapon.ship?.getWeaponGroupFor(weapon)?.let { weapon.ship?.weaponGroupsCopy?.indexOf(it) } ?: 0
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float = 1f

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = false

    override fun avoidDebris(): Boolean = true
}