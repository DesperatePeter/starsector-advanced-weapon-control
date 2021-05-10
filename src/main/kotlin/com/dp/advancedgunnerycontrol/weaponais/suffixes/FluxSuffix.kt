package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class FluxSuffix(weapon: WeaponAPI, private val fluxCap : Float) : SuffixBase(weapon) {
    override fun suppressFire(target: CombatEntityAPI?): Boolean {
        return weapon.ship.fluxLevel >= fluxCap
    }
}