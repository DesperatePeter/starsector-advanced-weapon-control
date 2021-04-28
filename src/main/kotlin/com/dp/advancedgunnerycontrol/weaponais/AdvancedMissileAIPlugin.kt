package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.MissileAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class AdvancedMissileAIPlugin (baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI){
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        val missile = (entity as? MissileAPI) ?: return Float.MAX_VALUE
        return computePriorityGeometrically(entity, predictedLocation)
    /*.let {
            if (!missile.isGuided) it*0.5f else it // prioritize unguided missiles
        }.let {
            it*1f/(entity.velocity.length().pow(0.5f)) // prioritize slow missiles
        }.let {
            it*missile.hitpoints/missile.damageAmount
        }*/
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        val targets = CombatUtils.getMissilesWithinRange(weapon.location, weapon.range + 200f)
        return targets.filterNotNull().filter { isWithinArc(it) && isHostile(it) && willBeInFiringRange(it)}
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(Settings.forceCustomAI) return false
        return (null != missile)
    }

    override fun isValid(): Boolean {
        return isPD(weapon)
    }

}
