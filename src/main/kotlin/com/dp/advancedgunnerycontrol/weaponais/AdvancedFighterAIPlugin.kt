package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.Settings
import com.dp.advancedgunnerycontrol.WeaponControlBasePlugin
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import kotlin.math.pow


class AdvancedFighterAIPlugin(baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI){
    val distToAngularDistEvalutionFactor = 1f/1000f
    override fun computeTargetPriority(entity: CombatEntityAPI): Float {
        val tgtPt = computePointToAimAt(entity)
        return angularDistanceFromWeapon(tgtPt) + distToAngularDistEvalutionFactor*linearDistanceFromWeapon(tgtPt)
        // TODO more sophisticated logic?
        //if ((weapon.type == WeaponAPI.WeaponType.BALLISTIC) && entity.shield?.isOn == true) times(0.5f)
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        var potentialTargets = CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f)
        return potentialTargets.filterNotNull().filter { it.isFighter && isHostile(it) && isWithinArc(it) && willBeInFiringRange(it)}
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(Settings.forceCustomAI) return false
        return ship?.isFighter ?: false
    }

    override fun isValid(): Boolean {
        return true
    }

}
