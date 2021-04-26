package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

class NoFighterAIPlugin(baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI, false) {
    override fun computeTargetPriority(entity: CombatEntityAPI): Float {
        return 0f
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return emptyList()
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        ship?.let { return !it.isFighter } ?: return true
    }

    override fun isValid() : Boolean{
        return isAimable(weapon)
    }
}