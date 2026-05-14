package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinition
import com.dp.advancedgunnerycontrol.weapontags.ParsedEditableWeaponTag

object EditableShipModeDefinitions {
    const val PARAM_THRESHOLD = SHIP_MODE_PARAM_THRESHOLD
    internal const val PARAM_PERSONALITY = SHIP_MODE_PARAM_PERSONALITY
    internal const val PARAM_DIRECT_RETREAT = SHIP_MODE_PARAM_DIRECT_RETREAT
    const val PARAM_VENT_SAFETY_FACTOR = SHIP_MODE_PARAM_VENT_SAFETY_FACTOR
    internal const val PARAM_VENT_AGGRESSIVE = SHIP_MODE_PARAM_VENT_AGGRESSIVE

    val definitions: List<EditableWeaponTagDefinition> = EditableShipModeDefinitionRegistry.definitions

    fun definitionById(id: String): EditableWeaponTagDefinition? =
        definitions.firstOrNull { it.id == id }

    fun definitionForTemplate(template: String): EditableWeaponTagDefinition? =
        definitions.firstOrNull { it.templateTag == template }

    fun definitionForMode(modeName: String): EditableWeaponTagDefinition? {
        val parsed = parseShipMode(modeName) ?: return null
        return definitions.firstOrNull { definition ->
            parseShipMode(definition.buildCanonicalTag(definition.defaultValues).canonicalTag ?: return@firstOrNull false)
                ?.mode == parsed.mode
        }
    }

    fun parse(modeName: String): ParsedEditableWeaponTag? =
        EditableShipModeDefinitionParser.parse(modeName)
}
