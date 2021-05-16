package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags



abstract class CustomShipAI(protected val baseAI: ShipAIPlugin, protected val ship: ShipAPI) : ShipAIPlugin {


    override fun setDoNotFireDelay(p0: Float) {
        ship.shipAI = baseAI
        setDoNotFireDelayImpl(p0)
        baseAI.setDoNotFireDelay(p0)
        ship.shipAI = this
    }

    protected abstract fun setDoNotFireDelayImpl(p0: Float)

    override fun forceCircumstanceEvaluation() {
        ship.shipAI = baseAI
        forceCircumstanceEvaluationImpl()
        baseAI.forceCircumstanceEvaluation()
        ship.shipAI = this
    }

    protected abstract fun forceCircumstanceEvaluationImpl()

    override fun advance(p0: Float) {
        ship.shipAI = baseAI
        advanceImpl(p0)
        baseAI.advance(p0)
        ship.shipAI = this
    }

    protected abstract fun advanceImpl(p0: Float)

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