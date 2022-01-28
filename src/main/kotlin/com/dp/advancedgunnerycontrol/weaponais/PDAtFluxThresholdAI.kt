package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f

class PDAtFluxThresholdAI (baseAI: AutofireAIPlugin, suffix: SuffixBase, private val threshold: Float)
    : SpecificAIPluginBase(baseAI, false, suffix) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float = 0f

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> = emptyList()

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(weapon.ship.fluxLevel < threshold) return true
        if (null != missile) return true
        return ship?.isFighter ?: false
    }

    override fun isBaseAIOverwritable(): Boolean = false

    override fun isValid(): Boolean {
        return isPD(weapon)
    }
}