package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class AvoidShieldsSuffix(weapon: WeaponAPI) : SuffixBase(weapon) {
    override fun modifyPriority(target: CombatEntityAPI?): Float {
        val tgtShip = (target as? ShipAPI) ?: return 1.0f
        return computeShieldFactor(tgtShip)
    }

    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        val tgtShip = (target as? ShipAPI) ?: return false
        return computeShieldFactor(tgtShip) < 1.1f && baseDecision
    }
}