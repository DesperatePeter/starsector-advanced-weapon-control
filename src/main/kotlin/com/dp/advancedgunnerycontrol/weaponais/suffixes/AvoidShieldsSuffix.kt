package com.dp.advancedgunnerycontrol.weaponais.suffixes

import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class AvoidShieldsSuffix(weapon: WeaponAPI) : SuffixBase(weapon) {
    override fun modifyPriority(target: CombatEntityAPI?): Float {
        val tgtShip = (target as? ShipAPI) ?: return 1.0f
        return computeShieldFactor(tgtShip, weapon)
    }

    override fun shouldFire(baseDecision: Boolean, target: CombatEntityAPI?): Boolean {
        val tgtShip = (target as? ShipAPI) ?: return false
        return computeShieldFactor(tgtShip, weapon) < 1.3f && baseDecision
    }
}