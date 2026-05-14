package com.dp.advancedgunnerycontrol.weaponais

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import java.util.WeakHashMap

object RecentBeamPressureTracker {
    private const val MAX_TRACKED_WINDOW_SECONDS = 15f

    private var trackedEngine: CombatEngineAPI? = null
    private var latestTime = 0f
    private val lastEnemyBeamHitByShip = WeakHashMap<ShipAPI, Float>()

    fun advance(engine: CombatEngineAPI) {
        if (trackedEngine !== engine) {
            trackedEngine = engine
            lastEnemyBeamHitByShip.clear()
        }

        latestTime = engine.getTotalElapsedTime(false)
        pruneStaleEntries()

        val beams = engine.beams ?: return
        for (beam in beams) {
            val target = beam.damageTarget as? ShipAPI ?: continue
            val source = beam.source ?: continue
            if (source === target) continue
            if (source.owner == target.owner) continue
            lastEnemyBeamHitByShip[target] = latestTime
        }
    }

    fun wasRecentlyHitByEnemyBeam(ship: ShipAPI, seconds: Float): Boolean {
        if (seconds <= 0f) return false
        val lastHit = lastEnemyBeamHitByShip[ship] ?: return false
        return latestTime - lastHit <= seconds
    }

    private fun pruneStaleEntries() {
        val staleBefore = latestTime - MAX_TRACKED_WINDOW_SECONDS
        val iterator = lastEnemyBeamHitByShip.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!entry.key.isAlive || entry.value < staleBefore) {
                iterator.remove()
            }
        }
    }
}
