package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.InEngineTagStorage
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.lang.ref.WeakReference

class TagBasedAI(baseAI: AutofireAIPlugin, tags: MutableList<WeaponAITagBase> = mutableListOf()) :
    SpecificAIPluginBase(baseAI) {

    var tags = mutableListOf<WeaponAITagBase>()
        set(value) {
            unregisterTagsForEveryFrameAdvance(field)
            field = value
            registerTagsForEveryFrameAdvance(field)
        }

    init {
        this.tags = tags
    }

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
        return tags.all { it.isBaseAiValid(tgt) }
    }

    override fun isBaseAIOverwritable(): Boolean {
        return tags.any { it.isBaseAiOverridable() }
    }

    override fun isValid(): Boolean = true

    override fun shouldFire(): Boolean {
        val baseDecision = super.shouldFire()
        if(tags.any { it.forceFire(targetEntity, targetPoint, baseDecision) }) return true
        if (!baseDecision) return false
        val tgt = targetEntity ?: return false
        val loc = targetPoint ?: return false
        return tags.all { it.shouldFire(tgt, loc) }
    }

    override fun shouldConsiderNeutralsAsFriendlies(): Boolean {
        return tags.any { it.avoidDebris() }
    }

    override fun advance(p0: Float) {
        super.advance(p0)
        tags.forEach { if(!it.advanceWhenTurnedOff) it.advance() }
    }

    companion object{
        fun unregisterTagsForEveryFrameAdvance(tags: List<WeaponAITagBase>){
            Global.getCombatEngine()?.let { engine ->
                if(!engine.customData.containsKey(Values.CUSTOM_ENGINE_TAGS_KEY)){
                    return
                }
                (engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] as? InEngineTagStorage)?.tags?.removeAll{ tags.contains(it.get())}
            }
        }
        fun registerTagsForEveryFrameAdvance(tags: List<WeaponAITagBase>){
            Global.getCombatEngine()?.let { engine ->
                if(!engine.customData.containsKey(Values.CUSTOM_ENGINE_TAGS_KEY)){
                    engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] = InEngineTagStorage()
                }
                (engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] as? InEngineTagStorage)?.tags?.addAll(tags.filter {
                    it.advanceWhenTurnedOff
                }.map {
                    WeakReference(it)
                })
            }
        }
        fun getTagsRegisteredForEveryFrameAdvancement() : List<WeaponAITagBase>{
            (Global.getCombatEngine()?.customData?.get(Values.CUSTOM_ENGINE_TAGS_KEY) as? InEngineTagStorage)?.let { store ->
                return store.tags.mapNotNull { it.get() }
            }
            return listOf()
        }
    }

}