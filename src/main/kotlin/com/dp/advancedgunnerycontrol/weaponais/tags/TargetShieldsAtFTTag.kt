package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class TargetShieldsFT(
    weapon: WeaponAPI,
    private val shieldThreshold: Float = Settings.targetShieldsThreshold(),
    private val fluxThreshold: Float = Settings.TargetShieldsFT()
) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return if (weapon.ship.fluxLevel <= fluxThreshold) {
            true
        } else {
            computeShieldFactor(entity, weapon) > shieldThreshold
        }
    }
    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val tgtShip = (entity as? ShipAPI) ?: return 1f
        return 1f/(computeShieldFactor(tgtShip, weapon) + 0.5f)
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        return if (weapon.ship.fluxLevel <= fluxThreshold) {
            true
        } else if (entity is ShipAPI) {
            if (Settings.ignoreFighterShields() && entity.isFighter) {
                true
            } else {
                val ttt = computeTimeToTravel(weapon, predictedLocation)
                computeShieldFactor(entity, weapon, ttt) > shieldThreshold
            }
        } else {
            false
        }
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}