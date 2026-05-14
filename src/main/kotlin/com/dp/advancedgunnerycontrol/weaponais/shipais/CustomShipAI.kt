package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f


open class CustomShipAI(
    protected val baseAI: ShipAIPlugin,
    protected val ship: ShipAPI,
    protected val commanders: List<ShipCommandGenerator>
) : ShipAIPlugin {

    private var fleetingCommands: MutableList<ShipCommandWrapper> = mutableListOf()
    private var fleetingBlockCommands: MutableSet<ShipCommand> = mutableSetOf()

    fun addFleetingCommand(cmd: ShipCommandWrapper) {
        fleetingCommands.add(cmd)
    }

    fun addFleetingBlockCommand(cmd: ShipCommand){
        fleetingBlockCommands.add(cmd)
    }

    fun containsFleetingCommand(cmd: ShipCommand, index: Int? = null, pos: Vector2f? = null): Boolean {
        return fleetingCommands.any {
            it.command == cmd
                    && (index?.equals(it.index) ?: true)
                    && (pos?.equals(it.position) ?: true)
        }
    }

    override fun setDoNotFireDelay(amount: Float) {
        ship.shipAI = baseAI
        baseAI.setDoNotFireDelay(amount)
        ship.shipAI = this
    }

    override fun forceCircumstanceEvaluation() {
        ship.shipAI = baseAI
        baseAI.forceCircumstanceEvaluation()
        ship.shipAI = this
    }

    override fun advance(amount: Float) {
        if(shouldNotOverrideShipAI(ship)){
            ship.shipAI = baseAI
            return
        }
        advanceImpl(amount)
        ship.shipAI = baseAI
        baseAI.advance(amount)
        ship.shipAI = this
    }

    protected fun advanceImpl(amount: Float) {
        commanders.forEach { commander ->
            commander.generateCommands().forEach { command ->
                ship.giveCommand(command.command, command.position, command.index)
            }
        }
        commanders.forEach { commander ->
            commander.blockCommands().forEach { command ->
                ship.blockCommandForOneFrame(command)
            }
        }
        fleetingCommands.forEach { command ->
            ship.giveCommand(command.command, command.position, command.index)
        }
        fleetingCommands.clear()
        fleetingBlockCommands.forEach { command ->
            ship.blockCommandForOneFrame(command)
        }
        fleetingBlockCommands.clear()
        if (commanders.any { it.shouldReevaluate() }) forceCircumstanceEvaluation()
    }

    override fun needsRefit(): Boolean {
        ship.shipAI = baseAI
        val result = baseAI.needsRefit()
        ship.shipAI = this
        return result
    }

    override fun getAIFlags(): ShipwideAIFlags? = baseAI.aiFlags

    override fun cancelCurrentManeuver() {
        ship.shipAI = baseAI
        baseAI.cancelCurrentManeuver()
        ship.shipAI = this
    }

    override fun getConfig(): ShipAIConfig? = baseAI.config
}
