package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.computeShieldFactor
import com.dp.advancedgunnerycontrol.weaponais.computeTimeToTravel
import com.dp.advancedgunnerycontrol.weaponais.computeArmorInImpactArea
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f

class AvoidArmorTag(weapon: WeaponAPI, private val shieldThreshold: Float = Settings.targetShieldsThreshold(),
                    private val armorThreshold: Float = 0.5f) : WeaponAITagBase(weapon) {
    override fun isValidTarget(entity: CombatEntityAPI): Boolean {
        return entity is ShipAPI
    }

    override fun computeTargetPriorityModifier(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val tgtShip = (entity as? ShipAPI) ?: return 1f
        val armorFactor = computeArmorInImpactArea(weapon,tgtShip)
        return (armorFactor + 0.1f)
    }

    override fun shouldFire(entity: CombatEntityAPI, predictedLocation: Vector2f): Boolean {
        val tgtShip = (entity as? ShipAPI) ?: return true
        val ttt = computeTimeToTravel(weapon, predictedLocation)
        val armorFactor = computeArmorInImpactArea(weapon,tgtShip)
        return computeShieldFactor(tgtShip, weapon, ttt) > shieldThreshold || armorFactor < armorThreshold
    }

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}