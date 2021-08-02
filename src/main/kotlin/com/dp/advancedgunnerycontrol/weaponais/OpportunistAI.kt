package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class OpportunistAI (baseAI: AutofireAIPlugin, suffix: SuffixBase) : SpecificAIPluginBase(baseAI, true, suffix) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return if (isOpportuneTarget(entity, predictedLocation, weapon)) {
            computeBasePriority(entity, predictedLocation)
        }else{
            10000.0f
        }
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return CombatUtils.getShipsWithinRange(weapon.location, weapon.range).filterNotNull().filter { !it.isFighter }
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean = false

    override fun isBaseAIOverwritable(): Boolean = true

    override fun isValid(): Boolean = true

    override fun shouldFire(): Boolean {
        if (!super.shouldFire()) return false
        return isOpportuneTarget(targetEntity, targetPoint, weapon)
    }

    override fun shouldConsiderNeutralsAsFriendlies(): Boolean = true

}