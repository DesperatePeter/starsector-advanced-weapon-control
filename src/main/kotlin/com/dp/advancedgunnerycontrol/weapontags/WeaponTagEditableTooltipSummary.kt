package com.dp.advancedgunnerycontrol.weapontags

internal fun withEditableParameterSummary(canonicalTag: String, tooltip: String): String {
    val parsed = EditableWeaponTagDefinitions.parse(canonicalTag) ?: return tooltip
    val definition = EditableWeaponTagDefinitions.definitionById(parsed.definitionId) ?: return tooltip
    val defaults = EditableWeaponTagDefinitions.defaultValuesFor(definition)
    val rows = EditableWeaponTagDefinitions.visibleParameters(definition, parsed.parameterValues).mapNotNull { parameter ->
        if (!showEditableParameterSummaryRow(definition, parsed, parameter)) return@mapNotNull null
        val value = parsed.parameterValues[parameter.id] ?: defaults[parameter.id] ?: return@mapNotNull null
        editableParameterSummaryRow(parameter, value)
    }
    if (rows.isEmpty()) return tooltip
    return tooltip.trimEnd() + "\n\n" + rows.joinToString("\n") { "- $it" }
}

private fun showEditableParameterSummaryRow(
    definition: EditableWeaponTagDefinition,
    parsed: ParsedEditableWeaponTag,
    parameter: EditableTagParameterDefinition,
): Boolean {
    if (definition.id != "hold_fire_flux_threshold") return true
    val isSoftFlux = parsed.parameterValues[EditableWeaponTagDefinitions.PARAM_FLUX_METRIC] == "SF"
    val ignoreIfBeamed = parsed.parameterValues[EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED]
        ?.toBooleanStrictOrNull() == true
    return when (parameter.id) {
        EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED -> isSoftFlux && ignoreIfBeamed
        EditableWeaponTagDefinitions.PARAM_BEAM_WINDOW -> isSoftFlux && ignoreIfBeamed
        else -> true
    }
}

private fun editableParameterSummaryRow(parameter: EditableTagParameterDefinition, value: String): String {
    return when (parameter) {
        is ChoiceParameter -> {
            val label = parameter.options.firstOrNull { it.id == value }?.label ?: value
            "${parameter.label}: $label"
        }
        is ToggleParameter -> "${parameter.label}: ${if (value.toBooleanStrictOrNull() == true) "On" else "Off"}"
        is NumberParameter -> "${parameter.label}: $value${parameter.suffix}"
        is DecimalParameter -> "${parameter.label}: $value${parameter.suffix}"
        is TextParameter -> "${parameter.label}: $value"
    }
}
