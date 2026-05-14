package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_CLEANUP_DAMAGE_CAP
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions.PARAM_THRESHOLD

internal fun noPdWasteDefinition(): EditableWeaponTagDefinition {
    val regex = Regex("AvoidPD\\(Waste>(\\d+)%(?:,Cap<(\\d+))?\\)")
    fun currentDefaults(): Map<String, String> = mapOf(
        PARAM_THRESHOLD to "40",
        PARAM_CLEANUP_DAMAGE_CAP to noPdWasteCleanupDamageCapDefault().toString(),
    )
    val parameters = listOf(
        NumberParameter(
            id = PARAM_THRESHOLD,
            label = "Waste above",
            minValue = 0,
            maxValue = 100,
            defaultValue = 40,
            suffix = "%",
        ),
        NumberParameter(
            id = PARAM_CLEANUP_DAMAGE_CAP,
            label = "Cleanup cap",
            minValue = 0,
            maxValue = 10000,
            defaultValue = noPdWasteCleanupDamageCapDefault(),
            markHiddenChange = true,
        )
    )
    return EditableWeaponTagDefinition(
        id = "no_pd_waste_threshold",
        family = "AvoidPD",
        templateTag = "AvoidPD(Waste>N%)",
        acceptedTemplateTags = setOf("AvoidPD(Waste>N%)", "NoPD(Waste>N%)"),
        parameters = parameters,
        defaultValues = currentDefaults(),
        parser = parser@{ canonicalTag ->
            val values = when {
                regex.matches(canonicalTag) -> {
                    val match = regex.matchEntire(canonicalTag) ?: return@parser null
                    val defaults = currentDefaults()
                    mapOf(
                        PARAM_THRESHOLD to match.groupValues[1],
                        PARAM_CLEANUP_DAMAGE_CAP to (match.groupValues.getOrNull(2).takeUnless { it.isNullOrBlank() }
                            ?: defaults.getValue(PARAM_CLEANUP_DAMAGE_CAP)),
                    )
                }
                else -> return@parser null
            }
            ParsedEditableWeaponTag(
                canonicalTag = canonicalTag,
                definitionId = "no_pd_waste_threshold",
                family = "AvoidPD",
                templateTag = "AvoidPD(Waste>N%)",
                parameterValues = values,
                exclusivityKeys = setOf("AvoidPD:Waste")
            )
        },
        builder = { values ->
            val dynamicDefaults = currentDefaults()
            val wasteText = values[PARAM_THRESHOLD] ?: dynamicDefaults.getValue(PARAM_THRESHOLD)
            val cleanupCapText = values[PARAM_CLEANUP_DAMAGE_CAP] ?: dynamicDefaults.getValue(PARAM_CLEANUP_DAMAGE_CAP)
            val waste = wasteText.toIntOrNull()
            val cleanupCap = cleanupCapText.toIntOrNull()
            val errors = mutableListOf<EditableTagValidationError>()
            if (waste == null || waste !in 0..100) {
                errors += EditableTagValidationError(PARAM_THRESHOLD, "Enter a whole number from 0 to 100.")
            }
            if (cleanupCap == null || cleanupCap !in 0..10000) {
                errors += EditableTagValidationError(PARAM_CLEANUP_DAMAGE_CAP, "Enter a whole number from 0 to 10000.")
            }
            if (errors.isNotEmpty()) {
                EditableTagBuildResult(canonicalTag = null, errors = errors)
            } else {
                val capSuffix = if (cleanupCapText == dynamicDefaults.getValue(PARAM_CLEANUP_DAMAGE_CAP)) {
                    ""
                } else {
                    ",Cap<$cleanupCap"
                }
                EditableTagBuildResult(
                    canonicalTag = "AvoidPD(Waste>$waste%$capSuffix)",
                    errors = emptyList()
                )
            }
        }
    )
}
