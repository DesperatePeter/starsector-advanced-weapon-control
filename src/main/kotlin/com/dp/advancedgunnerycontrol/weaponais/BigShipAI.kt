package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class BigShipAI(baseAI: AutofireAIPlugin, suffix: SuffixBase) : SpecificAIPluginBase(baseAI, suffix = suffix) {
    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return (entity as? ShipAPI)?.let {
            computeBasePriority(entity, predictedLocation) / bigness(it)
        } ?: Float.MAX_VALUE
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        return CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f).filterNotNull().filter {
            isBig(it)
        }
    }

    override fun isBaseAIOverwritable(): Boolean = true

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean = ship?.let { isBig(it) } ?: false

    override fun isValid(): Boolean = true
}