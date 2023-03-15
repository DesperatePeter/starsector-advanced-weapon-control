package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus

class ShieldsUpAI(ship: ShipAPI, private val fluxThreshold: Float) : ShipCommandGenerator(ship) {
    companion object {
        const val scanRange = 4000f
        const val scanFrequency = 20
        const val rangeSafetyBuffer = 200f
    }

    private var scanCounter = scanFrequency
    private var areEnemiesInRange = false
    override fun blockCommands(): List<ShipCommand> {
        if (!areEnemiesInRange()) {
            return emptyList()
        }
        return if (ship.shield?.isOn == true && (ship.fluxTracker?.fluxLevel ?: 1f) <= fluxThreshold) {
            listOf(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
        } else {
            emptyList()
        }
    }

    private fun areEnemiesInRange(): Boolean {
        fun areEnemiesWithinWeaponRange(): Boolean {
            return CombatUtils.getShipsWithinRange(ship.location, scanRange).filter { it.owner == 1 }.filterNotNull()
                .any {
                    val dist: Float = (ship.location - it.location).length()
                    it.allWeapons?.any { w -> w.range + rangeSafetyBuffer >= dist } == true
                }
        }
        if (--scanCounter <= 0) {
            scanCounter = scanFrequency
            areEnemiesInRange = areEnemiesWithinWeaponRange()
        }
        return areEnemiesInRange
    }
}