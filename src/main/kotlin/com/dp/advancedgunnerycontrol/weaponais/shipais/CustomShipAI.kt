package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags



open class CustomShipAI(protected val baseAI: ShipAIPlugin, protected val ship: ShipAPI, protected val commanders : List<ShipCommandGenerator>) : ShipAIPlugin {

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

    protected fun advanceImpl(p0: Float){
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
        if(commanders.any { it.shouldReevaluate() }) forceCircumstanceEvaluation()
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