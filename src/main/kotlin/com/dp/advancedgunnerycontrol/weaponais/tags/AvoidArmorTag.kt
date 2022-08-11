package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.dp.advancedgunnerycontrol.weaponais.computeArmorInImpactArea
import com.dp.advancedgunnerycontrol.weaponais.computeWeaponEffectivenessVsArmor
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class AvoidArmorTag(weapon: WeaponAPI, private val armorThreshold: Float = 0.33f,
                    private val shieldThreshold: Float = Settings.targetShieldsThreshold()) : WeaponAITagBase(weapon) {

    override fun isBaseAiValid(entity: CombatEntityAPI): Boolean {
        return computeArmorEffectiveness(entity) > armorThreshold
    }

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return 1f / (computeArmorEffectiveness(entity) + 0.1f)
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        val ttt = computeTimeToTravel(weapon, predictedLocation)
        val armorEffectiveness = computeArmorEffectiveness(entity)
        return (computeShieldFactor(entity, weapon, ttt) > shieldThreshold) || (armorEffectiveness > armorThreshold)
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false

    private fun computeArmorEffectiveness(entity: CombatEntityAPI) : Float{
        val armorInArc = computeArmorInImpactArea(weapon,entity)
        return computeWeaponEffectivenessVsArmor(weapon, armorInArc)
    }
}