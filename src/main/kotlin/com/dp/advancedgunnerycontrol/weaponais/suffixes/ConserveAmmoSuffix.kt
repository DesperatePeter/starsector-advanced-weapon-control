package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.ammoLevel
import com.dp.advancedgunnerycontrol.weaponais.isOpportuneTarget
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.WeaponAPI

class ConserveAmmoSuffix(weapon: WeaponAPI) : SuffixBase(weapon) {
    private val ammoThreshold = Settings.conserveAmmo()
    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        if(ammoLevel(weapon) < ammoThreshold) {
            return isOpportuneTarget(target, target?.location, weapon) && baseDecision
        }
        return baseDecision
    }
}