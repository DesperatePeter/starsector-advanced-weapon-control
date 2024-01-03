package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.PointNavigator
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.evaluatePointDanger
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.findSafePoint
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.getInvertedCommands
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class StayFarAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    val navigator = PointNavigator(ship)
    private val intervalTracker = IntervalUtil(50f, 50f)
    private var point: Vector2f? = null
    private val vecToPoint
        get() = point?.minus(ship.location)
    private var currentDanger = 0f
    // if danger is smaller than some arbitrary, very small, number
    private val maxDanger = ship.fleetMember.deploymentPointsCost / 10000f
    private val goBackwardsDanger
        get() = ship.fleetMember.deploymentPointsCost / 1000f / (ship.hullLevel + 0.01f)
    private var shouldGoBackwards = false
    private val isSafe
        get() = currentDanger < maxDanger
    private var commandsToBlock = listOf<ShipCommand>()


    override fun generateCommands(): List<ShipCommandWrapper> {
        intervalTracker.advance(1f)
        commandsToBlock = emptyList()
        if(intervalTracker.intervalElapsed() || point == null){
            point = findSafePoint(ship)
            val enemies = CombatUtils.getShipsWithinRange(ship.location, 2500f).filter { it.owner == 1 }
            currentDanger = evaluatePointDanger(ship.location, enemies)
            shouldGoBackwards = (currentDanger > goBackwardsDanger) && isBackwardsSafer(enemies)
        }
        if(isSafe) return emptyList()
        val commands = point?.let { p ->
            if(shouldGoBackwards){
                navigator.generateCommandsToMoveToPointBackwards(p)
            }else{
                navigator.generateCommandsToMoveToPoint(p)
            }
        } ?: emptyList()
        commandsToBlock = commands.map { getInvertedCommands(it.command) }.flatten().toSet().toList()
        return commands
    }

    override fun blockCommands(): List<ShipCommand> {
        return commandsToBlock
    }

    private fun isBackwardsSafer(enemies: List<ShipAPI>): Boolean{
        val vec = Vector2f(vecToPoint)
        vec.scale(1000f)
        return 0.8f * currentDanger > evaluatePointDanger(ship.location + vec, enemies)
    }
}