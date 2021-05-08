package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

open class SuffixBase(protected val weapon: WeaponAPI) {
    open fun suppressFire() : Boolean = false
    open fun modifyPriority(target: CombatEntityAPI) : Float = 1.0f
}