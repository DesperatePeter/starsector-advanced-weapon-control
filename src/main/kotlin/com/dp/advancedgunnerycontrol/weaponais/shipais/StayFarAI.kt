package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.PointNavigator
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.evaluateDangerAtPoint
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.findSafePoint
import com.dp.advancedgunnerycontrol.weaponais.shipais.utils.getInvertedCommands
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class StayFarAI(ship: ShipAPI) : ShipCommandGenerator(ship) {
    private val navigator = PointNavigator(ship)
    private val intervalTracker = IntervalUtil(50f, 50f)
    private var point: Vector2f? = null
    private val vecToPoint
        get() = point?.minus(ship.location)
    private var currentDanger = 0f
    // arbitrary, small, number
    private val maxDanger = ship.fleetMember.deploymentPointsCost / 20f
    // low value = more likely to move backwards
    private val goBackwardsDanger
        get(): Float {
            val dp = ship.fleetMember.deploymentPointsCost
            val shieldTypeModifier = if(ship.shield?.type == ShieldAPI.ShieldType.OMNI || (ship.shield?.activeArc ?: 0f) >= 359f) 5f else 1f
            val hullLevelModifier = (ship.hullLevel + 0.1f)
            val facingModifier = point?.let { p ->
                val facingToTarget = Misc.normalizeAngle(Misc.getAngleInDegrees(ship.location, p))
                val facing = ship.facing
                val deltaFacingAbs = abs(facing - facingToTarget)
                1.5f - abs(sin(0.5f * deltaFacingAbs * 2f * PI.toFloat() / 360f))
            } ?: 1f
            return 0.25f * dp * shieldTypeModifier * hullLevelModifier * facingModifier

        }
    private var shouldGoBackwards = false
    private val isSafe
        get() = currentDanger < maxDanger
    private var commandsToBlock = listOf<ShipCommand>()


    override fun generateCommands(): List<ShipCommandWrapper> {
        intervalTracker.advance(1f)
        if(intervalTracker.intervalElapsed() || point == null){
            point = findSafePoint(ship)
            val enemies = CombatUtils.getShipsWithinRange(ship.location, 3000f).filter { it.owner == 1 }
            currentDanger = evaluateDangerAtPoint(ship.location, enemies)
            shouldGoBackwards = (currentDanger > goBackwardsDanger) && isBackwardsSafer(enemies)
        }
        if(isSafe){
            commandsToBlock = emptyList()
            return emptyList()
        }
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
        return 0.8f * currentDanger > evaluateDangerAtPoint(ship.location + vec, enemies)
    }
}