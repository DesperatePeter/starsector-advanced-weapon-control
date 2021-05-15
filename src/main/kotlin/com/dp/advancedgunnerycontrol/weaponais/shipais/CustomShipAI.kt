package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAIPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags

open class CustomShipAI(protected val baseAI: ShipAIPlugin, protected val ship: ShipAPI) : ShipAIPlugin {
    override fun setDoNotFireDelay(p0: Float) = baseAI.setDoNotFireDelay(p0)

    override fun forceCircumstanceEvaluation() = baseAI.forceCircumstanceEvaluation()

    override fun advance(p0: Float) = baseAI.advance(p0)

    override fun needsRefit(): Boolean = baseAI.needsRefit()

    override fun getAIFlags(): ShipwideAIFlags = baseAI.aiFlags

    override fun cancelCurrentManeuver() = baseAI.cancelCurrentManeuver()

    override fun getConfig(): ShipAIConfig = baseAI.config
}