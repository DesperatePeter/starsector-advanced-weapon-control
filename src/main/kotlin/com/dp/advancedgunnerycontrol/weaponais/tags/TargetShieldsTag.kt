package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class TargetShieldsTag(weapon: WeaponAPI, private val threshold: Float = Settings.targetShieldsThreshold()) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return computeShieldFactor(entity, weapon) > threshold
    }
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val tgtShip = (solution.targetEntity as? ShipAPI) ?: return 1f
        return 1f/(computeShieldFactor(tgtShip, weapon) + 0.5f)
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        val tgtShip = (solution.targetEntity as? ShipAPI) ?: return false
        if (Settings.ignoreFighterShields() && tgtShip.isFighter) {
            return true
        }
        val ttt = computeTimeToTravel(weapon, solution.targetPoint)
        return computeShieldFactor(tgtShip, weapon, ttt) > threshold
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}