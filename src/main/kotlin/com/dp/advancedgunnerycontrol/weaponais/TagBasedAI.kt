package com.dp.advancedgunnerycontrol.weaponais

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.utils.InEngineTagStorage
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.dp.advancedgunnerycontrol.weaponais.tags.DisableTagsRuntime
import com.dp.advancedgunnerycontrol.weaponais.shipais.PreAimShipModeRuntime
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.combat.CombatUtils
import java.lang.ref.WeakReference

data class WeaponDecisionSnapshot(
    val weapon: WeaponAPI,
    val solution: FiringSolution?,
    val baseDecision: Boolean,
    val timestamp: Float
)

class TagBasedAI(baseAI: AutofireAIPlugin, tags: MutableList<WeaponAITagBase> = mutableListOf()) :
    SpecificAIPluginBase(baseAI) {

    var decisionSnapshot: WeaponDecisionSnapshot? = null
        private set

    var tags = listOf<WeaponAITagBase>()
        set(value) {
            unregisterTagsForEveryFrameAdvance(field)
            field = value
            registerTagsForEveryFrameAdvance(field)
        }

    init {
        this.tags = tags
    }

    override fun computeTargetPriority(solution: FiringSolution): Float {
        val activeTags = activeTags()
        val basePriority = computeBasePriority(solution)
        var groupTargetModifier = 1.0f
        activeTags.forEach { tag ->
            groupTargetModifier *= tag.computeTargetPriorityModifierForGroupTargetChoice(solution)
        }
        val groupTargetPriority = basePriority * groupTargetModifier
        activeTags.forEach { it.observeTargetPriority(solution, groupTargetPriority) }

        var targetModifier = 1.0f
        activeTags.forEach { tag ->
            targetModifier *= tag.computeTargetPriorityModifier(solution)
        }
        return basePriority * targetModifier
    }

    override fun getRelevantEntitiesOutOfRange(): List<CombatEntityAPI> {
        val activeTags = activeTags()
        val seen = HashSet<CombatEntityAPI>()
        val result = ArrayList<CombatEntityAPI>()
        fun addIfNew(entity: CombatEntityAPI) {
            if (seen.add(entity)) result += entity
        }
        for (tag in activeTags) {
            tag.addFarAwayTargets().forEach(::addIfNew)
        }
        PreAimShipModeRuntime.outOfRangeTargetsFor(weapon).forEach(::addIfNew)
        return result
    }

    override fun getRelevantEntitiesWithinRange(): List<CombatEntityAPI> {
        val activeTags = activeTags()
        val seen = HashSet<CombatEntityAPI>()
        val result = ArrayList<CombatEntityAPI>()
        fun addIfValid(entity: CombatEntityAPI?) {
            if (entity == null || entity in seen) return
            if (!activeTags.all { it.isValidTarget(entity) }) return
            seen += entity
            result += entity
        }
        CombatUtils.getShipsWithinRange(weapon.location, weapon.range + 200f).forEach(::addIfValid)
        CombatUtils.getMissilesWithinRange(weapon.location, weapon.range + 200f).forEach(::addIfValid)
        return result
    }

    override fun isBaseAITargetValid(ship: ShipAPI?, missile: MissileAPI?): Boolean {
        val baseTarget = ship as? CombatEntityAPI ?: missile as? CombatEntityAPI ?: return false
        return activeTags().all { it.isBaseAiValid(baseTarget) }
    }

    override fun isBaseAIOverwritable(): Boolean {
        return activeTags().any { it.isBaseAiOverridable() }
    }

    override fun isValid(): Boolean = true

    override fun shouldFire(): Boolean {
        val activeTags = activeTags()
        val baseDecision = super.shouldFire()
        if (activeTags.isEmpty()) return baseDecision
        if (activeTags.any { it.forceFire(solution, baseDecision) }) return true
        val sol = activeTags.firstNotNullOfOrNull { it.overrideFiringSolution() } ?: solution ?: return false
        activeTags.forEach { it.observeFiringDecision(sol, baseDecision) }
        if (!baseDecision && activeTags.none { it.overrideBaseFireDecision(sol, baseDecision) }) return false
        val synchronizedReleaseActive = activeTags.any { it.isSynchronizedReleaseActive(sol) }
        val shouldFire = activeTags.all {
            if (synchronizedReleaseActive) {
                it.shouldFireDuringSynchronizedRelease(sol)
            } else {
                it.shouldFire(sol)
            }
        }
        if (shouldFire) {
            activeTags.forEach { it.onFireAllowed(sol) }
        }
        return shouldFire
    }

    override fun shouldConsiderNeutralsAsFriendlies(): Boolean {
        return activeTags().any { it.avoidDebris() }
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        decisionSnapshot = WeaponDecisionSnapshot(
            weapon,
            solution,
            super.shouldFire(),
            Global.getCombatEngine()?.getTotalElapsedTime(false) ?: 0f
        )
        val activeTags = activeTags()
        activeTags.forEach { if (!it.advanceWhenTurnedOff) it.advance() }
        activeTags.firstNotNullOfOrNull { it.overrideFiringSolution() }?.let { solution = it }
    }

    private fun activeTags(): List<WeaponAITagBase> =
        if (DisableTagsRuntime.isDisabled(weapon)) emptyList() else tags

    companion object {
        fun unregisterTagsForEveryFrameAdvance(tags: List<WeaponAITagBase>) {
            Global.getCombatEngine()?.let { engine ->
                if (!engine.customData.containsKey(Values.CUSTOM_ENGINE_TAGS_KEY)) {
                    return
                }
                (engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] as? InEngineTagStorage)?.tags?.removeAll {
                    tags.contains(it.get())
                }
            }
        }

        fun registerTagsForEveryFrameAdvance(tags: List<WeaponAITagBase>) {
            Global.getCombatEngine()?.let { engine ->
                if (!engine.customData.containsKey(Values.CUSTOM_ENGINE_TAGS_KEY)) {
                    engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] = InEngineTagStorage()
                }
                (engine.customData[Values.CUSTOM_ENGINE_TAGS_KEY] as? InEngineTagStorage)?.tags?.addAll(tags.filter {
                    it.advanceWhenTurnedOff
                }.map {
                    WeakReference(it)
                })
            }
        }

        fun getTagsRegisteredForEveryFrameAdvancement(): List<WeaponAITagBase> {
            (Global.getCombatEngine()?.customData?.get(Values.CUSTOM_ENGINE_TAGS_KEY) as? InEngineTagStorage)?.let { store ->
                val liveTags = ArrayList<WeaponAITagBase>(store.tags.size)
                val iterator = store.tags.iterator()
                while (iterator.hasNext()) {
                    val tag = iterator.next().get()
                    if (tag == null) {
                        iterator.remove()
                    } else {
                        liveTags += tag
                    }
                }
                return liveTags
            }
            return listOf()
        }
    }

}
