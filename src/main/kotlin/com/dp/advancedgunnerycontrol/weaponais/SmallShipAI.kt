package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class SmallShipAI(baseAI: AutofireAIPlugin) : SpecificAIPluginBase(baseAI) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return (entity as? ShipAPI)?.let {
            computeBasePriority(entity, predictedLocation) * bigness(it)
        } ?: Float.MAX_VALUE
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f)
            .filterNotNull().filter { isSmall(it) }
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean = ship?.let { isSmall(it) } ?: false

    override fun isBaseAIOverwritable(): Boolean = true

    override fun isValid(): Boolean = true
}