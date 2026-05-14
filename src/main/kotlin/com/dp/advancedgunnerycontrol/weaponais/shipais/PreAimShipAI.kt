package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

class PreAimShipAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    override fun generateCommands(): List<ShipCommandWrapper> {
        PreAimShipModeRuntime.markActive(ship)
        return emptyList()
    }
}

object PreAimShipModeRuntime {
    private const val ACTIVE_AT_KEY = "AGC_PRE_AIM_ACTIVE_AT"
    private const val ACTIVE_GRACE_SECONDS = 0.05f

    fun markActive(ship: ShipAPI) {
        ship.setCustomData(ACTIVE_AT_KEY, now())
    }

    fun outOfRangeTargetsFor(weapon: WeaponAPI): List<CombatEntityAPI> {
        val ship = weapon.ship ?: return emptyList()
        if (!isActive(ship)) return emptyList()
        val target = ship.shipTarget as? CombatEntityAPI ?: return emptyList()
        if (target.owner == ship.owner || target.owner == 100) return emptyList()
        return listOf(target)
    }

    private fun isActive(ship: ShipAPI): Boolean {
        val activeAt = ship.customData[ACTIVE_AT_KEY] as? Float ?: return false
        val engine = Global.getCombatEngine() ?: return false
        val grace = maxOf(ACTIVE_GRACE_SECONDS, engine.elapsedInLastFrame * 3f)
        return now() - activeAt <= grace
    }

    private fun now(): Float =
        Global.getCombatEngine()?.getTotalElapsedTime(false) ?: 0f
}
