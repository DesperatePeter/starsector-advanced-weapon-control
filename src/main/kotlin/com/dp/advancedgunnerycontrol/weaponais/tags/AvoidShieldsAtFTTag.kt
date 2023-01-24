package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

// Allows targeting of anything when flux < fluxThreshold, otherwise avoids shields. Always prioritises by target shield factor
class AvoidShieldsAtFTTag(
    weapon: WeaponAPI,
    private val shieldThreshold: Float = Settings.avoidShieldsThreshold(),
    private val fluxThreshold: Float = Settings.avoidShieldsAtFT()
) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return if (weapon.ship.fluxLevel <= fluxThreshold) {
            true
        } else {
            computeShieldFactor(entity, weapon) < shieldThreshold
        }
    }
    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        return computeShieldFactor(solution.targetEntity, weapon) + 0.1f
    }

    override fun shouldFire(solution: FiringSolution): Boolean {
        return if (weapon.ship.fluxLevel <= fluxThreshold) {
            true
        } else if (solution.targetEntity is ShipAPI) {
            if (Settings.ignoreFighterShields() && solution.targetEntity.isFighter) {
                true
            } else {
                val ttt = computeTimeToTravel(weapon, solution.targetPoint)
                computeShieldFactor(solution.targetEntity, weapon, ttt)  < shieldThreshold
            }
        } else {
            false
        }

    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}