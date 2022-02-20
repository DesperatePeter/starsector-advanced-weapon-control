package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f

class TagBasedAI(baseAI: AutofireAIPlugin, var tags: MutableList<WeaponAITagBase> = mutableListOf()) :
    SpecificAIPluginBase(baseAI) {

    override fun computeTargetPriority(entity: CombatEntityAPI, predictedLocation: Vector2f): Float {
        return computeBasePriority(entity, predictedLocation) *
                (tags.map { it.computeTargetPriorityModifier(entity, predictedLocation) }.reduceOrNull(Float::times)
                    ?: 1.0f)
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        val ships = CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f).filterNotNull()
        val missiles = CombatUtils.getMissilesWithinRange(weapon.location, weapon.range + 200f).filterNotNull()
        val entities: MutableList<CombatEntityAPI> = (ships + missiles) as MutableList<CombatEntityAPI>
        return entities.filter { entity -> tags.all { it.isValidTarget(entity) } }
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        val tgt = ship as? CombatEntityAPI ?: missile as? CombatEntityAPI ?: return false
        return tags.all { it.isValidTarget(tgt) }
    }

    override fun isBaseAIOverwritable(): Boolean {
        return tags.any { it.isBaseAiOverridable() }
    }

    override fun isValid(): Boolean = true

    override fun shouldFire(): Boolean {
        if (!super.shouldFire()) return false
        val tgt = targetEntity ?: return false
        val loc = targetPoint ?: return false
        return tags.all { it.shouldFire(tgt, loc) }
    }

    override fun shouldConsiderNeutralsAsFriendlies(): Boolean {
        return tags.any { it.avoidDebris() }
    }
}