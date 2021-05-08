package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.fs.starfarer.api.combat.WeaponAPI

class FluxSuffix(weapon: WeaponAPI, private val fluxCap : Float) : SuffixBase(weapon) {
    override fun suppressFire(): Boolean {
        return weapon.ship.fluxLevel >= fluxCap
    }
}