package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.WeaponControlBasePlugin
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import kotlin.math.pow


class AdvancedFighterAIPlugin(baseAI : AutofireAIPlugin) : SpecificAIPluginBase(baseAI){
    override fun computeTargetPriority(entity: CombatEntityAPI): Float {
        return angularDistanceFromWeapon(computePointToAimAt(1, entity))
        // TODO more sophisticated logic?
        //if ((weapon.type == WeaponAPI.WeaponType.BALLISTIC) && entity.shield?.isOn == true) times(0.5f)
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        var potentialTargets = CombatUtils.getShipsWithinRange(weapon.location, weapon.range)
        return potentialTargets.filterNotNull().filter { it.isFighter && isHostile(it) && isWithinArc(it)}
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        if(WeaponControlBasePlugin.forceCustomAI) return false
        return ship?.isFighter ?: false
    }

    override fun isValid(): Boolean {
        return true
    }

}
