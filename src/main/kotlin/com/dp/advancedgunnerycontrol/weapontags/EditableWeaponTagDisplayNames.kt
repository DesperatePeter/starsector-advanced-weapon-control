package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_FLUX_METRIC
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TOTAL_FLUX_CAP

internal fun displayNameForEditableWeaponTag(tag: String): String {
    val canonicalTag = canonicalizeWeaponTagName(tag)
    val parsed = EditableWeaponTagDefinitions.parse(canonicalTag) ?: return canonicalTag
    val definition = EditableWeaponTagDefinitions.definitionById(parsed.definitionId) ?: return canonicalTag
    val hiddenChanged = hasHiddenChangedParameters(parsed, definition)
    fluxConditionDisplayName(parsed, definition, hiddenChanged)?.let { return it }
    if (!hiddenChanged) {
        return definition.buildCanonicalTag(parsed.parameterValues).canonicalTag
            ?.takeIf { canonicalTag.contains("Ignore<") }
            ?: canonicalTag
    }
    parseSyncTagOptions(parsed.canonicalTag)?.displayName()?.let { return it }
    return "*${parsed.family}"
}

private fun fluxConditionDisplayName(
    parsed: ParsedEditableWeaponTag,
    definition: EditableWeaponTagDefinition,
    hiddenChanged: Boolean,
): String? {
    if (
        parsed.definitionId !in setOf(
            "hold_fire_flux_threshold",
            "force_fire_flux_threshold",
            "avoid_shield_flux_threshold",
            "target_shield_flux_threshold",
        )
    ) return null
    val metric = parsed.parameterValues[PARAM_FLUX_METRIC] ?: return null
    val threshold = parsed.parameterValues[PARAM_THRESHOLD] ?: return null
    val comparator = if (parsed.definitionId == "force_fire_flux_threshold") "<" else ">"
    val marker = if (hiddenChanged) "*" else ""
    if (parsed.definitionId == "hold_fire_flux_threshold") {
        return "$marker${parsed.family}($metric$comparator$threshold%)"
    }
    val defaultCap = EditableWeaponTagDefinitions.defaultValuesFor(definition)[PARAM_TOTAL_FLUX_CAP]
    val cap = parsed.parameterValues[PARAM_TOTAL_FLUX_CAP]
        ?.takeIf { totalFluxCapAppliesToMetric(definition, metric) }
        ?.takeIf { defaultCap == null || it != defaultCap }
        ?.let { ",TF<$it%" }
        .orEmpty()
    return "$marker${parsed.family}($metric$comparator$threshold%$cap)"
}

private fun hasHiddenChangedParameters(
    parsed: ParsedEditableWeaponTag,
    definition: EditableWeaponTagDefinition,
): Boolean {
    for (parameter in EditableWeaponTagDefinitions.visibleParameters(definition)) {
        if (!parameter.useHiddenChangeMarker) continue
        val currentValue = parsed.parameterValues[parameter.id] ?: continue
        val defaultValue = EditableWeaponTagDefinitions.defaultValuesFor(definition)[parameter.id] ?: continue
        if (currentValue != defaultValue) return true
    }
    return false
}

internal fun totalFluxCapAppliesToMetric(
    definition: EditableWeaponTagDefinition,
    metric: String,
): Boolean {
    return definition.id != "hold_fire_flux_threshold" || metric == "SF"
}
