package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import kotlin.math.pow


class AdvancedFighterAIPlugin(baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI){
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return computePriorityGeometrically(entity, predictedLocation).let {
            if ((weapon.type == WeaponAPI.WeaponType.BALLISTIC) && entity.shield?.isOn == true) it*0.5f else it
        }.let {
            it*1f/(entity.velocity.length().pow(0.5f)) // prioritize slow fighters
        }
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f).filterNotNull()
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(Settings.forceCustomAI) return false
        return ship?.isFighter ?: false
    }

    override fun isValid(): Boolean {
        return true
    }

}
