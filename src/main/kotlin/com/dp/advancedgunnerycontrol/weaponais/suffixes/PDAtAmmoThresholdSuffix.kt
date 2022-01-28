package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.ammoLevel
import com.dp.advancedgunnerycontrol.weaponais.isValidPDTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PDAtAmmoThresholdSuffix (weapon: WeaponAPI, private val ammoThreshold : Float = 0.8f) : SuffixBase(weapon) {
    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        if(ammoLevel(weapon) <= ammoThreshold){
            return baseDecision && isValidPDTarget(target)
        }
        return baseDecision
    }

    override fun modifyPriority(target: CombatEntityAPI?): Float {
        if(ammoLevel(weapon) <= ammoThreshold && isValidPDTarget(target)){
            return 0.01f
        }
        return 1f
    }
}