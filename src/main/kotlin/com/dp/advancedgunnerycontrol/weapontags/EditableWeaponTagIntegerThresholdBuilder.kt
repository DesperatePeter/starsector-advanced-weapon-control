package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionDefaults
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionParameters
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromMatch
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

internal fun integerThresholdDefinition(
    id: String,
    family: String,
    representativeTemplateTag: String,
    acceptedTemplateTags: Set<String>,
    label: String,
    prefix: String,
    suffix: String,
    thresholdMinimum: Int,
    thresholdMaximum: Int,
    defaultThreshold: Int,
    exclusivityPrefix: String,
    includeDamageTypeExclusions: Boolean = false,
): EditableWeaponTagDefinition {
    val regex = if (includeDamageTypeExclusions && suffix.endsWith(")")) {
        val suffixBeforeClose = suffix.dropLast(1)
        Regex("${Regex.escape(prefix)}(\\d+)${Regex.escape(suffixBeforeClose)}$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
    } else {
        Regex("${Regex.escape(prefix)}(\\d+)${Regex.escape(suffix)}")
    }
    val parameters = mutableListOf<EditableTagParameterDefinition>(
        NumberParameter(
            id = PARAM_THRESHOLD,
            label = label,
            minValue = thresholdMinimum,
            maxValue = thresholdMaximum,
            defaultValue = defaultThreshold,
            suffix = if (suffix.contains("%")) "%" else ""
        )
    )
    if (includeDamageTypeExclusions) {
        parameters += damageTypeExclusionParameters
    }
    val defaults = mutableMapOf(PARAM_THRESHOLD to defaultThreshold.toString())
    if (includeDamageTypeExclusions) {
        defaults += damageTypeExclusionDefaults()
    }

    return EditableWeaponTagDefinition(
        id = id,
        family = family,
        templateTag = representativeTemplateTag,
        acceptedTemplateTags = acceptedTemplateTags,
        parameters = parameters,
        defaultValues = defaults,
        parser = parser@{ canonicalTag ->
            val match = regex.matchEntire(canonicalTag) ?: return@parser null
            val threshold = match.groupValues[1]
            val damageTypeExclusions = if (includeDamageTypeExclusions) {
                damageTypeExclusionsFromMatch(match.groupValues.getOrNull(2))
                    ?: return@parser null
            } else {
                DamageTypeExclusions.NONE
            }
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = id,
                family = family,
                templateTag = tagNameToRegexName(canonicalTag),
                parameterValues = buildMap {
                    put(PARAM_THRESHOLD, threshold)
                    if (includeDamageTypeExclusions) {
                        putAll(damageTypeExclusionValues(damageTypeExclusions))
                    }
                },
                exclusivityKeys = setOf(exclusivityPrefix)
            )
        },
        builder = { values ->
            val thresholdText = values[PARAM_THRESHOLD] ?: defaultThreshold.toString()
            val threshold = thresholdText.toIntOrNull()
            val damageTypeExclusions = damageTypeExclusionsFromValues(values)
            val errors = mutableListOf<EditableTagValidationError>()
            if (threshold == null || threshold !in thresholdMinimum..thresholdMaximum) {
                errors += EditableTagValidationError(
                    PARAM_THRESHOLD,
                    "Enter a whole number from $thresholdMinimum to $thresholdMaximum."
                )
            }

            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else {
                val builtSuffix = if (includeDamageTypeExclusions && suffix.endsWith(")")) {
                    "${suffix.dropLast(1)}${damageTypeExclusions.suffix()})"
                } else {
                    suffix
                }
                EditableTagBuildResult(canonicalTag = "$prefix$threshold$builtSuffix", errors = emptyList())
            }
        }
    )
}
