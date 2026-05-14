package com.dp.advancedgunnerycontrol.weaponais.shipais

import com.fs.starfarer.api.combat.ShipAPI

class PersonalityOverrideShipAI(
    ship: ShipAPI,
    personality: String,
) : ShipCommandGenerator(ship) {
    private val personality = personality.lowercase()
    private var needsReevaluation = false

    override fun generateCommands(): List<ShipCommandWrapper> {
        val config = ship.shipAI?.config ?: return emptyList()
        PersonalityOverrideShipModeRuntime.captureOriginalOverride(ship, config.personalityOverride)
        if (config.personalityOverride != personality) {
            config.personalityOverride = personality
            needsReevaluation = true
        }
        return emptyList()
    }

    override fun shouldReevaluate(): Boolean {
        val result = needsReevaluation
        needsReevaluation = false
        return result
    }
}

object PersonalityOverrideShipModeRuntime {
    private const val ORIGINAL_PERSONALITY_OVERRIDE_KEY = "AGC_ORIGINAL_PERSONALITY_OVERRIDE"
    private const val NULL_PERSONALITY_OVERRIDE = "<null>"

    fun captureOriginalOverride(ship: ShipAPI, currentOverride: String?) {
        if (!ship.customData.containsKey(ORIGINAL_PERSONALITY_OVERRIDE_KEY)) {
            ship.setCustomData(ORIGINAL_PERSONALITY_OVERRIDE_KEY, currentOverride ?: NULL_PERSONALITY_OVERRIDE)
        }
    }

    fun syncForShipModeAssignment(ship: ShipAPI, personalityModeActive: Boolean) {
        if (personalityModeActive) return
        val original = ship.customData.remove(ORIGINAL_PERSONALITY_OVERRIDE_KEY) as? String ?: return
        ship.shipAI?.config?.personalityOverride = original.takeUnless { it == NULL_PERSONALITY_OVERRIDE }
        ship.shipAI?.forceCircumstanceEvaluation()
    }
}
