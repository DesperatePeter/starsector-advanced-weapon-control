package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class TargetShieldsSuffix (weapon: WeaponAPI) : SuffixBase(weapon) {
    override fun modifyPriority(target: CombatEntityAPI?): Float {
        val tgtShip = (target as? ShipAPI) ?: return 1.0f
        return 1f/(computeShieldFactor(tgtShip, weapon) + 0.0001f)
    }

    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        val tgtShip = (target as? ShipAPI) ?: return false
        return computeShieldFactor(tgtShip, weapon) > 0.5f && baseDecision
    }
}