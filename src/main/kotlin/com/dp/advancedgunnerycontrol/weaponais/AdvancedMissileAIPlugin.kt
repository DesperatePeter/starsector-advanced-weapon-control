package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.WeaponControlBasePlugin
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.MissileAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class AdvancedMissileAIPlugin (baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI){
    override fun computeTargetPriority(entity: CombatEntityAPI): Float {
        return angularDistanceFromWeapon(computePointToAimAt(1, entity))
        // TODO: More sophisticated logic
//        var preliminaryPriority = (weapon.location - entity.location).length() // distance
//        if((entity as MissileAPI).isArmed) preliminaryPriority*=0.1f // prioritize armed missiles
//        return if (entity.isGuided){ // prioritize unguided missiles
//            preliminaryPriority / (entity.damage.damage + entity.empAmount*0.25f) * entity.hitpoints
//        }else{
//            preliminaryPriority / (entity.damage.damage + entity.empAmount*0.25f) * entity.hitpoints * 0.5f
//        }
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        val targets = CombatUtils.getMissilesWithinRange(weapon.location, weapon.range)
        return targets.filterNotNull().filter { isWithinArc(it) && isHostile(it) }
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(WeaponControlBasePlugin.forceCustomAI) return false
        return (null != missile)
    }

    override fun isValid(): Boolean {
        return isPD(weapon)
    }

}
