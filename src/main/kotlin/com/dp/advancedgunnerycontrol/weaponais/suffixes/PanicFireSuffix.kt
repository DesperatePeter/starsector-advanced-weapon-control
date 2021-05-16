package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.isValidPDTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PanicFireSuffix (weapon: WeaponAPI, private val hullThreshold : Float = 0.5f) : SuffixBase(weapon) {
    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        if(weapon.ship.hullLevel <= hullThreshold){
            return true
        }
        return baseDecision
    }

}