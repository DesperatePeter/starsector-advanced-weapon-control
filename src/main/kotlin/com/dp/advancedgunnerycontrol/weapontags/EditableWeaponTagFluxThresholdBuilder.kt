package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionDefaults
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionParameters
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromMatch
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDamageExclusionSupport.damageTypeExclusionsFromValues
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_BEAM_WINDOW
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_FLUX_METRIC
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_IGNORE_IF_BEAMED
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TARGET_SHIELD_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_TOTAL_FLUX_CAP
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

private const val HOLD_FIRE_BEAM_WINDOW_DEFAULT_SECONDS = 2f

private val fluxMetricOptions = listOf(
    TagParameterOption("TF", "Total flux"),
    TagParameterOption("SF", "Soft flux"),
    TagParameterOption("HF", "Hard flux")
)

internal fun fluxThresholdDefinition(
    id: String,
    family: String,
    representativeTemplateTag: String,
    acceptedTemplateTags: Set<String>,
    comparator: String,
    thresholdMinimum: Int,
    thresholdMaximum: Int,
    defaultMetric: String,
    defaultThreshold: Int,
    exclusivityPrefix: String,
    includeTotalFluxCap: Boolean = false,
    totalFluxCapAllowedMetricIds: Set<String> = setOf("TF", "SF", "HF"),
    includeRecentBeamException: Boolean = false,
    targetShieldThresholdComparator: String? = null,
    defaultTargetShieldThresholdProvider: (() -> Int)? = null,
    includeDamageTypeExclusions: Boolean = false,
): EditableWeaponTagDefinition {
    val defaultTargetShieldThreshold = defaultTargetShieldThresholdProvider?.invoke()
    val totalFluxCapPattern = if (includeTotalFluxCap) "(?:,TF<(\\d+)%)?" else ""
    val beamWindowPattern = if (includeRecentBeamException) "(?:,Beam<(\\d+(?:\\.\\d+)?)s)?" else ""
    val damageTypeExclusionPattern = if (includeDamageTypeExclusions) OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN else ""
    val shieldThresholdPattern = targetShieldThresholdComparator
        ?.let { "(?:,S${Regex.escape(it)}(\\d+)%)?" }
        ?: ""
    val regex = Regex("${Regex.escape(family)}\\((TF|SF|HF)${Regex.escape(comparator)}(\\d+)%$shieldThresholdPattern$totalFluxCapPattern$beamWindowPattern$damageTypeExclusionPattern\\)")
    val parameters = mutableListOf<EditableTagParameterDefinition>(
        ChoiceParameter(
            id = PARAM_FLUX_METRIC,
            label = "Flux type",
            options = fluxMetricOptions,
            defaultOptionId = defaultMetric
        ),
        NumberParameter(
            id = PARAM_THRESHOLD,
            label = "Threshold",
            minValue = thresholdMinimum,
            maxValue = thresholdMaximum,
            defaultValue = defaultThreshold,
            suffix = "%"
        )
    )
    if (targetShieldThresholdComparator != null && defaultTargetShieldThreshold != null) {
        parameters += NumberParameter(
            id = PARAM_TARGET_SHIELD_THRESHOLD,
            label = "Target shield",
            minValue = 0,
            maxValue = 100,
            defaultValue = defaultTargetShieldThreshold,
            suffix = "%",
            markHiddenChange = true,
        )
    }
    if (includeTotalFluxCap) {
        parameters += NumberParameter(
            id = PARAM_TOTAL_FLUX_CAP,
            label = "Max TF",
            minValue = 1,
            maxValue = 100,
            defaultValue = totalFluxCapDefaultPercent(),
            suffix = "%",
            markHiddenChange = id == "hold_fire_flux_threshold",
        )
    }
    if (includeRecentBeamException) {
        parameters += ToggleParameter(
            id = PARAM_IGNORE_IF_BEAMED,
            label = "Ignore if beamed",
            defaultValue = false,
            markHiddenChange = true,
        )
        parameters += DecimalParameter(
            id = PARAM_BEAM_WINDOW,
            label = "Beam window",
            minValue = 0.1f,
            maxValue = 10f,
            defaultValue = HOLD_FIRE_BEAM_WINDOW_DEFAULT_SECONDS,
            minorStep = 0.1f,
            majorStep = 1f,
            decimalPlaces = 2,
            suffix = "s",
            markHiddenChange = true,
        )
    }
    if (includeDamageTypeExclusions) {
        parameters += damageTypeExclusionParameters
    }
    val defaults = mutableMapOf(
        PARAM_FLUX_METRIC to defaultMetric,
        PARAM_THRESHOLD to defaultThreshold.toString()
    )
    if (includeTotalFluxCap) {
        defaults[PARAM_TOTAL_FLUX_CAP] = totalFluxCapDefaultPercent().toString()
    }
    if (includeRecentBeamException) {
        defaults[PARAM_IGNORE_IF_BEAMED] = false.toString()
        defaults[PARAM_BEAM_WINDOW] = formatDecimalTagNumber(HOLD_FIRE_BEAM_WINDOW_DEFAULT_SECONDS)
    }
    if (targetShieldThresholdComparator != null && defaultTargetShieldThreshold != null) {
        defaults[PARAM_TARGET_SHIELD_THRESHOLD] = defaultTargetShieldThreshold.toString()
    }
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
            val metric = match.groupValues[1]
            val threshold = match.groupValues[2]
            var nextGroupIndex = 3
            val targetShieldThreshold = if (targetShieldThresholdComparator != null) {
                match.groupValues.getOrNull(nextGroupIndex++)
                    ?.takeIf { it.isNotBlank() }
                    ?: defaultTargetShieldThresholdProvider?.invoke()?.toString()
            } else {
                null
            }
            val totalFluxCap = if (includeTotalFluxCap) {
                val parsedCap = match.groupValues.getOrNull(nextGroupIndex++)
                    ?.takeIf { it.isNotBlank() }
                if (metric in totalFluxCapAllowedMetricIds) {
                    parsedCap ?: totalFluxCapDefaultPercent().toString()
                } else {
                    null
                }
            } else {
                null
            }
            val beamWindow = if (includeRecentBeamException) {
                match.groupValues.getOrNull(nextGroupIndex++)
                    ?.takeIf { it.isNotBlank() }
                    ?.toFloatOrNull()
            } else {
                null
            }
            val damageTypeExclusions = if (includeDamageTypeExclusions) {
                damageTypeExclusionsFromMatch(match.groupValues.getOrNull(nextGroupIndex))
                    ?: return@parser null
            } else {
                DamageTypeExclusions.NONE
            }
            if (beamWindow != null && metric != "SF") return@parser null
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = id,
                family = family,
                templateTag = tagNameToRegexName(canonicalTag),
                parameterValues = buildMap {
                    put(PARAM_FLUX_METRIC, metric)
                    put(PARAM_THRESHOLD, threshold)
                    if (targetShieldThreshold != null) put(PARAM_TARGET_SHIELD_THRESHOLD, targetShieldThreshold)
                    if (totalFluxCap != null) put(PARAM_TOTAL_FLUX_CAP, totalFluxCap)
                    if (includeRecentBeamException) {
                        put(PARAM_IGNORE_IF_BEAMED, (beamWindow != null).toString())
                        put(
                            PARAM_BEAM_WINDOW,
                            formatDecimalTagNumber(beamWindow ?: HOLD_FIRE_BEAM_WINDOW_DEFAULT_SECONDS)
                        )
                    }
                    if (includeDamageTypeExclusions) {
                        putAll(damageTypeExclusionValues(damageTypeExclusions))
                    }
                },
                // Family alone is too broad: TF and SF variants can be compatible if the tag declares distinct slots.
                exclusivityKeys = setOf("$exclusivityPrefix:$metric")
            )
        },
        builder = { values ->
            val metric = values[PARAM_FLUX_METRIC] ?: defaultMetric
            val thresholdText = values[PARAM_THRESHOLD] ?: defaultThreshold.toString()
            val currentDefaultTargetShieldThreshold = defaultTargetShieldThresholdProvider?.invoke()
            val currentDefaultTotalFluxCap = totalFluxCapDefaultPercent()
            val targetShieldThresholdText =
                values[PARAM_TARGET_SHIELD_THRESHOLD] ?: currentDefaultTargetShieldThreshold?.toString()
            val totalFluxCapText = values[PARAM_TOTAL_FLUX_CAP] ?: currentDefaultTotalFluxCap.toString()
            val ignoreIfBeamed = values[PARAM_IGNORE_IF_BEAMED]?.toBooleanStrictOrNull() ?: false
            val beamWindowText = values[PARAM_BEAM_WINDOW] ?: formatDecimalTagNumber(HOLD_FIRE_BEAM_WINDOW_DEFAULT_SECONDS)
            val damageTypeExclusions = damageTypeExclusionsFromValues(values)
            val errors = mutableListOf<EditableTagValidationError>()
            val totalFluxCapApplies = includeTotalFluxCap && metric in totalFluxCapAllowedMetricIds

            if (metric !in fluxMetricOptions.map { it.id }) {
                errors += EditableTagValidationError(PARAM_FLUX_METRIC, "Choose one of TF, SF, or HF.")
            }

            val threshold = thresholdText.toIntOrNull()
            if (threshold == null || threshold !in thresholdMinimum..thresholdMaximum) {
                errors += EditableTagValidationError(
                    PARAM_THRESHOLD,
                    "Enter a whole number from $thresholdMinimum to $thresholdMaximum."
                )
            }
            val totalFluxCap = totalFluxCapText.toIntOrNull()
            if (totalFluxCapApplies && (totalFluxCap == null || totalFluxCap !in 1..100)) {
                errors += EditableTagValidationError(
                    PARAM_TOTAL_FLUX_CAP,
                    "Enter a whole number from 1 to 100."
                )
            }
            val targetShieldThreshold = targetShieldThresholdText?.toIntOrNull()
            if (
                targetShieldThresholdComparator != null &&
                (targetShieldThreshold == null || targetShieldThreshold !in 0..100)
            ) {
                errors += EditableTagValidationError(
                    PARAM_TARGET_SHIELD_THRESHOLD,
                    "Enter a whole number from 0 to 100."
                )
            }
            val beamWindow = beamWindowText.toFloatOrNull()
            if (
                includeRecentBeamException &&
                metric == "SF" &&
                ignoreIfBeamed &&
                (beamWindow == null || beamWindow !in 0.1f..10f)
            ) {
                errors += EditableTagValidationError(
                    PARAM_BEAM_WINDOW,
                    "Enter seconds from 0.1 to 10."
                )
            }

            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else {
                val shieldSuffix = if (
                    targetShieldThresholdComparator != null &&
                    targetShieldThreshold != null &&
                    targetShieldThreshold != currentDefaultTargetShieldThreshold
                ) {
                    ",S$targetShieldThresholdComparator$targetShieldThreshold%"
                } else {
                    ""
                }
                val capSuffix = if (
                    totalFluxCapApplies &&
                    totalFluxCap != null &&
                    totalFluxCap != currentDefaultTotalFluxCap
                ) {
                    ",TF<$totalFluxCap%"
                } else {
                    ""
                }
                val beamSuffix = if (
                    includeRecentBeamException &&
                    metric == "SF" &&
                    ignoreIfBeamed &&
                    beamWindow != null
                ) {
                    ",Beam<${formatDecimalTagNumber(beamWindow)}s"
                } else {
                    ""
                }
                val damageTypeSuffix = if (includeDamageTypeExclusions) damageTypeExclusions.suffix() else ""
                EditableTagBuildResult(
                    canonicalTag = "$family($metric$comparator$threshold%$shieldSuffix$capSuffix$beamSuffix$damageTypeSuffix)",
                    errors = emptyList()
                )
            }
        }
    )
}
