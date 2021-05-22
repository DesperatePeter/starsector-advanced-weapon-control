package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

class NoFighterAIPlugin(baseAI: AutofireAIPlugin, suffix: SuffixBase) : SpecificAIPluginBase(baseAI, false, suffix) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return 0f
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return emptyList()
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        return ship?.let { !it.isFighter } ?: true
    }

    override fun isBaseAIOverwritable(): Boolean = false

    override fun isValid(): Boolean {
        return true
    }
}