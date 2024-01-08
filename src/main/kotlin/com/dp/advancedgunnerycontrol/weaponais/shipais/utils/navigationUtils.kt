package com.dp.advancedgunnerycontrol.weaponais.shipais.utils

import com.dp.advancedgunnerycontrol.weaponais.shipais.ShipCommandWrapper
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import kotlin.math.abs

const val SP_SCAN_RANGE = 8000f
const val NUMBER_OF_EXPLORATION_RAYS = 36
const val NUMBER_OF_EXPLORATION_POINTS_PER_RAY = 8
const val EXPLORATION_RAY_LENGTH = 4000f
const val EXPLORATION_RAY_SEGMENT_LENGTH = EXPLORATION_RAY_LENGTH / NUMBER_OF_EXPLORATION_POINTS_PER_RAY.toFloat()
const val POINT_SAFETY_EVAL_MAX_RANGE = 3000f

fun findSafePoint(ship: ShipAPI): Vector2f?{
    val enemies = CombatUtils.getShipsWithinRange(ship.location, SP_SCAN_RANGE).filter { it.owner == 1 }
    // create a list with star pattern of trajectories (lists of points) around current location
    val possibleTrajectories = (0..NUMBER_OF_EXPLORATION_RAYS).map { i ->
        val direction = Misc.getUnitVectorAtDegreeAngle(i.toFloat() / NUMBER_OF_EXPLORATION_RAYS.toFloat() * 360f)
        (0..NUMBER_OF_EXPLORATION_POINTS_PER_RAY).map { n ->
            val l = n.toFloat() * EXPLORATION_RAY_SEGMENT_LENGTH
            ship.location + Vector2f(l * direction.x, l * direction.y)
        }
    }
    return possibleTrajectories.minByOrNull { t ->
        t.map { evaluatePointDanger(it, enemies) }.sum()
    }?.last()
}

fun evaluatePointDanger(point: Vector2f, enemies: List<ShipAPI>, maxDist: Float = POINT_SAFETY_EVAL_MAX_RANGE ): Float{
    return enemies.filter {
        (it.location - point).length() < maxDist
    }.map {
        enemy -> ((enemy.fleetMember?.deploymentPointsCost ?: 0f) + 0.5f) / ((enemy.location - point).length() + 250f)
    }.sum()
}

fun getInvertedCommands(command: ShipCommand): List<ShipCommand>{
    return when(command){
        ShipCommand.ACCELERATE -> listOf(ShipCommand.DECELERATE, ShipCommand.ACCELERATE_BACKWARDS)
        ShipCommand.DECELERATE -> listOf(ShipCommand.ACCELERATE, ShipCommand.ACCELERATE_BACKWARDS)
        ShipCommand.TURN_LEFT -> listOf(ShipCommand.TURN_RIGHT)
        ShipCommand.TURN_RIGHT -> listOf(ShipCommand.TURN_LEFT)
        ShipCommand.ACCELERATE_BACKWARDS -> listOf(ShipCommand.ACCELERATE, ShipCommand.DECELERATE)
        ShipCommand.STRAFE_LEFT -> listOf(ShipCommand.STRAFE_RIGHT)
        ShipCommand.STRAFE_RIGHT -> listOf(ShipCommand.STRAFE_LEFT)
        else -> listOf()
    }
}

class PointNavigator(private val ship: ShipAPI){
    companion object{
        const val DEFAULT_APPROACH_DISTANCE = 50f
        const val FACING_TOLERANCE = 4f
        const val ANGULAR_VEL_TOLERANCE = 1f
        const val DELTA_FACING_THRESHOLD = 45f // should start accelerating if deltaFacing is below threshold
        private fun commandFromDirection(isPositiveDirection: Boolean): ShipCommand{
            return if(isPositiveDirection) ShipCommand.TURN_LEFT else ShipCommand.TURN_RIGHT
        }
    }

    private val timeToComeToStop
        get() = ship.velocity.length() / ship.deceleration
    private val distToComeToStop
        get() = 0.5f * ship.velocity.length() * timeToComeToStop

    fun generateCommandsToMoveToPoint(point: Vector2f): List<ShipCommandWrapper>{
        return generateCommandsImpl(point, ShipCommand.ACCELERATE, 0f)
    }

    fun generateCommandsToMoveToPointBackwards(point: Vector2f): List<ShipCommandWrapper>{
        return generateCommandsImpl(point, ShipCommand.ACCELERATE_BACKWARDS, 180f)
    }

    private fun generateCommandsImpl(point: Vector2f, accelCommand: ShipCommand, angleOffset: Float): List<ShipCommandWrapper>{
        val distance = (ship.location - point).length()
        val toReturn = mutableListOf<ShipCommandWrapper>()
        if(distance < DEFAULT_APPROACH_DISTANCE) return emptyList()
        val facingToTarget = Misc.normalizeAngle(Misc.getAngleInDegrees(ship.location, point) + angleOffset)
        val facing = ship.facing
        val deltaFacingAbs = abs(facing - facingToTarget)

        val otherWayShorter = deltaFacingAbs > 180f + 10f
        val shouldTurnInPositiveDirection = facingToTarget > facing && !otherWayShorter

        if(Misc.normalizeAngle(deltaFacingAbs) > FACING_TOLERANCE){
            val command = if(shouldTurnInPositiveDirection) ShipCommand.TURN_LEFT else ShipCommand.TURN_RIGHT
            toReturn.add(ShipCommandWrapper(command))
        }

        determineTurningCommand(deltaFacingAbs, shouldTurnInPositiveDirection)?.let {
            toReturn.add(ShipCommandWrapper(it))
        }

        if(Misc.normalizeAngle(deltaFacingAbs) < DELTA_FACING_THRESHOLD){
            val command = if(distance < distToComeToStop) ShipCommand.DECELERATE else accelCommand
            toReturn.add(ShipCommandWrapper(command))
        }

        return toReturn
    }

    private fun determineTurningCommand(deltaFacingAbs: Float, isPositiveDirection: Boolean): ShipCommand?{
        val deltaFacing = if(isPositiveDirection) deltaFacingAbs else -deltaFacingAbs
        if(deltaFacingAbs < FACING_TOLERANCE && abs(ship.angularVelocity) < ANGULAR_VEL_TOLERANCE) return null
        val isTurningInCorrectDirection = deltaFacing * ship.angularVelocity > 0
        if(!isTurningInCorrectDirection) return commandFromDirection(isPositiveDirection)
        val timeToNeutralAngularVel = abs(ship.angularVelocity) / ship.turnAcceleration
        val projectedDeltaFacing = 0.5f * ship.angularVelocity * timeToNeutralAngularVel
        return if(abs(projectedDeltaFacing) < deltaFacingAbs){
            commandFromDirection(isPositiveDirection)
        }else{
            commandFromDirection(!isPositiveDirection)
        }
    }
}



