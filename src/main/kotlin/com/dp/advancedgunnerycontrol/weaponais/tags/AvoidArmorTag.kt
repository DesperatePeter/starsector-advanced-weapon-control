package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class AvoidArmorTag(weapon: WeaponAPI, private val armorThreshold: Float = 0.33f,
                    private val shieldThreshold: Float = Settings.targetShieldsThreshold()) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        // Only trust the base AI if it's well above threshold
        return computeArmorEffectiveness(entity)  > (armorThreshold * 2f) || (entity as? ShipAPI)?.isFighter == true
    }

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return 1f / (computeArmorEffectiveness(entity, predictedLocation) + 0.1f)
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        val ttt = computeTimeToTravel(weapon, predictedLocation)
        val armorEffectiveness = computeArmorEffectiveness(entity, predictedLocation)
        return computeShieldFactor(entity, weapon, ttt) > shieldThreshold || armorEffectiveness > armorThreshold || (entity as? ShipAPI)?.isFighter == true
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun computeArmorEffectiveness(entity: CombatEntityAPI, predictedLocation: Vector2f? = null) : Float{
        val armorInArc = computeOuterLayerArmorInImpactArea(weapon,entity, predictedLocation)
        return computeWeaponEffectivenessVsArmor(weapon, armorInArc)
    }
}