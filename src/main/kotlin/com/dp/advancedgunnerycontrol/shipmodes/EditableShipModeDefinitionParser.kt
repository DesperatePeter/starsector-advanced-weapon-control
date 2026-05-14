package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions.PARAM_DIRECT_RETREAT
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions.PARAM_PERSONALITY
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions.PARAM_VENT_AGGRESSIVE
import com.dp.advancedgunnerycontrol.shipmodes.EditableShipModeDefinitions.PARAM_VENT_SAFETY_FACTOR
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinition
import com.dp.advancedgunnerycontrol.weapontags.ParsedEditableWeaponTag

internal object EditableShipModeDefinitionParser {
    fun parse(modeName: String): ParsedEditableWeaponTag? {
        val parsed = parseShipMode(modeName) ?: return null
        val definition = EditableShipModeDefinitions.definitionForMode(parsed.canonicalName) ?: return null
        if (parsed.mode == ShipModes.PERSONALITY_OVERRIDE) {
            return parsePersonalityMode(parsed, definition)
        }
        if (parsed.mode == ShipModes.VENT) {
            return parseVentMode(parsed, definition)
        }
        val threshold = when (parsed.mode) {
            ShipModes.RETREAT -> parsed.hullThreshold
            ShipModes.CR_RETREAT -> null
            ShipModes.HULL_RETREAT -> null
            else -> parsed.fluxThreshold
        }
        if (parsed.mode == ShipModes.CR_RETREAT || parsed.mode == ShipModes.HULL_RETREAT) {
            return parseMultiThresholdRetreatMode(parsed, definition)
        }
        val thresholdValue = threshold ?: return null
        return ParsedEditableWeaponTag(
            canonicalTag = parsed.canonicalName,
            definitionId = definition.id,
            family = definition.family,
            templateTag = definition.templateTag,
            parameterValues = mapOf(PARAM_THRESHOLD to (thresholdValue * 100f).toInt().toString()),
            exclusivityKeys = setOf("ShipMode:${parsed.mode.name}"),
        )
    }

    private fun parsePersonalityMode(
        parsed: ParsedShipMode,
        definition: EditableWeaponTagDefinition,
    ): ParsedEditableWeaponTag {
        val personality = parsed.personality ?: "Steady"
        return ParsedEditableWeaponTag(
            canonicalTag = parsed.canonicalName,
            definitionId = definition.id,
            family = definition.family,
            templateTag = definition.templateTag,
            parameterValues = mapOf(PARAM_PERSONALITY to personality.lowercase()),
            exclusivityKeys = setOf("ShipMode:${parsed.mode.name}"),
        )
    }

    private fun parseVentMode(
        parsed: ParsedShipMode,
        definition: EditableWeaponTagDefinition,
    ): ParsedEditableWeaponTag {
        return ParsedEditableWeaponTag(
            canonicalTag = parsed.canonicalName,
            definitionId = definition.id,
            family = definition.family,
            templateTag = definition.templateTag,
            parameterValues = mapOf(
                PARAM_THRESHOLD to ((parsed.fluxThreshold ?: Settings.ventFluxThreshold()) * 100f).toInt().toString(),
                PARAM_VENT_SAFETY_FACTOR to formatVentParameter(parsed.ventSafetyFactor ?: Settings.ventSafetyFactor()),
                PARAM_VENT_AGGRESSIVE to parsed.aggressiveVent.toString(),
            ),
            exclusivityKeys = setOf("ShipMode:${parsed.mode.name}"),
        )
    }

    private fun parseMultiThresholdRetreatMode(
        parsed: ParsedShipMode,
        definition: EditableWeaponTagDefinition,
    ): ParsedEditableWeaponTag {
        val slots = when (parsed.mode) {
            ShipModes.CR_RETREAT -> parsed.crThresholds
            ShipModes.HULL_RETREAT -> parsed.hullThresholds
            else -> emptyList()
        }
        fun enabledValue(index: Int): String = (slots.getOrNull(index) != null).toString()
        fun thresholdValue(index: Int): String =
            (slots.getOrNull(index)?.let { (it * 100f).toInt() } ?: multiThresholdRetreatDefaults[index]).toString()
        return ParsedEditableWeaponTag(
            canonicalTag = parsed.canonicalName,
            definitionId = definition.id,
            family = definition.family,
            templateTag = definition.templateTag,
            parameterValues = multiThresholdRetreatDefaults.indices.flatMap { index ->
                listOf(
                    enabledParameterId(index) to enabledValue(index),
                    thresholdParameterId(index) to thresholdValue(index),
                )
            }.toMap() + mapOf(PARAM_DIRECT_RETREAT to (parsed.directRetreat == true).toString()),
            exclusivityKeys = setOf("ShipMode:${parsed.mode.name}"),
        )
    }
}
