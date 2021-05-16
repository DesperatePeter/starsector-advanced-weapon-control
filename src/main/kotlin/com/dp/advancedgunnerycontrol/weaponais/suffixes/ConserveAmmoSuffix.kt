package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.isOpportuneTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ConserveAmmoSuffix(weapon: WeaponAPI) : SuffixBase(weapon) {
    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        if(ammoLevel() < 0.9f) {
            return isOpportuneTarget(target, target?.location, weapon) && baseDecision
        }
        return baseDecision
    }
}