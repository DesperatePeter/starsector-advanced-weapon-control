package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import org.lwjgl.util.vector.Vector2f

data class ShipCommandWrapper(val command: ShipCommand, val position: Vector2f? = null, val index: Int = 0)

open class ShipCommandGenerator(protected val ship: ShipAPI) {
    open fun generateCommands(): List<ShipCommandWrapper> = emptyList()
    open fun shouldReevaluate(): Boolean = false
    open fun blockCommands(): List<ShipCommand> = emptyList()
}