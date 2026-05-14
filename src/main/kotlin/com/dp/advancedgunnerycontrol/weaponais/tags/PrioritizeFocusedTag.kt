package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.FiringSolution
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PrioritizeFocusedTag(
    weapon: WeaponAPI,
    private val multiplierOverride: Float? = null
) : WeaponAITagBase(weapon) {
    private fun multiplier(): Float = multiplierOverride ?: Settings.prioXModifier()

    override fun computeTargetPriorityModifier(solution: FiringSolution): Float {
        val targetShip = solution.target as? ShipAPI ?: return 1f
        val focusedAllies = FocusedTargetCache.focusCountFor(weapon.ship, targetShip)
        if (focusedAllies <= 0) return 1f
        return 1f / (1f + focusedAllies * (multiplier() - 1f))
    }

    override fun shouldFire(solution: FiringSolution): Boolean = true

    override fun isBaseAiOverridable(): Boolean = true

    override fun avoidDebris(): Boolean = false
}

private data class FocusedTargetSnapshot(
    val timestamp: Float,
    val owner: Int,
    val counts: Map<ShipAPI, Int>
)

private object FocusedTargetCache {
    fun focusCountFor(observer: ShipAPI?, target: ShipAPI): Int {
        val engine = Global.getCombatEngine() ?: return 0
        val owner = observer?.owner ?: return 0
        val timestamp = engine.getTotalElapsedTime(false)
        val snapshots = focusedSnapshots(engine)
        val existing = snapshots[owner]
        val snapshot = if (existing != null && existing.timestamp == timestamp && existing.owner == owner) {
            existing
        } else {
            val counts = mutableMapOf<ShipAPI, Int>()
            for (ally in engine.ships) {
                if (ally == null || ally.owner != owner || ally.isFighter) continue
                val allyTarget = ally.shipTarget ?: continue
                if (allyTarget.owner == owner || allyTarget.owner == 100) continue
                counts[allyTarget] = (counts[allyTarget] ?: 0) + 1
            }
            FocusedTargetSnapshot(timestamp, owner, counts).also { snapshots[owner] = it }
        }
        val ownTargetCount = if (observer.shipTarget == target) 1 else 0
        return ((snapshot.counts[target] ?: 0) - ownTargetCount).coerceAtLeast(0)
    }

    private fun focusedSnapshots(engine: CombatEngineAPI): MutableMap<Int, FocusedTargetSnapshot> {
        val raw = engine.customData[Values.CUSTOM_ENGINE_FOCUSED_TARGET_CACHE_KEY]
        val existing = raw as? MutableMap<*, *>
        if (existing != null && existing.all { (key, value) ->
                key is Int && value is FocusedTargetSnapshot
            }
        ) {
            @Suppress("UNCHECKED_CAST")
            return existing as MutableMap<Int, FocusedTargetSnapshot>
        }

        val created = mutableMapOf<Int, FocusedTargetSnapshot>()
        engine.customData[Values.CUSTOM_ENGINE_FOCUSED_TARGET_CACHE_KEY] = created
        return created
    }
}
