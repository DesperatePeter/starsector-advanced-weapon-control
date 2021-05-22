package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.isValidPDTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PDAtFluxThresholdSuffix (weapon: WeaponAPI, private val fluxThreshold : Float = 0.5f) : SuffixBase(weapon) {
    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        if(weapon.ship.fluxLevel >= fluxThreshold){
            return baseDecision && isValidPDTarget(target)
        }
        return baseDecision
    }

    override fun modifyPriority(target: CombatEntityAPI?): Float {
        if(weapon.ship.fluxLevel >= fluxThreshold && isValidPDTarget(target)){
            return 0.01f
        }
        return 1f
    }
}