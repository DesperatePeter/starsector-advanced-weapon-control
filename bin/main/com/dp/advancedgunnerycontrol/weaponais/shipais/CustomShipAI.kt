package com.dp.advancedgunnerycontrol.weaponais.shipais

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

    override fun setDoNotFireDelay(p0: Float) {
        ship.shipAI = baseAI
        baseAI.setDoNotFireDelay(p0)
        ship.shipAI = this
    }

    override fun forceCircumstanceEvaluation() {
        ship.shipAI = baseAI
        baseAI.forceCircumstanceEvaluation()
        ship.shipAI = this
    }

    override fun advance(p0: Float) {
        advanceImpl(p0)
        ship.shipAI = baseAI
        baseAI.advance(p0)
        ship.shipAI = this
    }

    protected fun advanceImpl(p0: Float) {
        commanders.forEach { cmdr ->
            cmdr.generateCommands().forEach {
                ship.giveCommand(it.command, it.position, it.index)
            }
        }
        commanders.forEach { cmdr ->
            cmdr.blockCommands().forEach {
                ship.blockCommandForOneFrame(it)
            }
        }
        fleetingCommands.forEach {
            ship.giveCommand(it.command, it.position, it.index)
        }
        fleetingCommands.clear()
        fleetingBlockCommands.forEach {
            ship.blockCommandForOneFrame(it)
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