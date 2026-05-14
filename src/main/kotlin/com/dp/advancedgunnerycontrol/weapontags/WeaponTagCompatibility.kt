package com.dp.advancedgunnerycontrol.weapontags

import com.fs.starfarer.api.fleet.FleetMemberAPI

private const val HARD_PD_ONLY_TEMPLATE = "PD"

private fun isHardPdOnlyTemplate(template: String): Boolean = template == HARD_PD_ONLY_TEMPLATE

private fun isTargetShieldTemplate(template: String): Boolean = template.startsWith("TargetShield")

private enum class ShieldTargetingKind {
    NO_SHIELD,
    AVOID_SHIELD,
    TARGET_SHIELD,
}

private data class ShieldTargetingSpec(
    val kind: ShieldTargetingKind,
    val thresholdPercent: Int? = null,
)

private fun shieldTargetingSpec(tag: String): ShieldTargetingSpec? {
    val canonicalTag = canonicalizeWeaponTagName(tag)
    if (tagNameToRegexName(canonicalTag) == "NoShield") {
        return ShieldTargetingSpec(ShieldTargetingKind.NO_SHIELD)
    }
    val parsed = EditableWeaponTagDefinitions.parse(canonicalTag) ?: return null
    val threshold = parsed.parameterValues[EditableWeaponTagDefinitions.PARAM_TARGET_SHIELD_THRESHOLD]?.toIntOrNull()
    return when (parsed.definitionId) {
        "avoid_shield_target_threshold",
        "avoid_shield_flux_threshold" ->
            ShieldTargetingSpec(ShieldTargetingKind.AVOID_SHIELD, threshold ?: avoidShieldDefaultPercent())
        "target_shield_target_threshold",
        "target_shield_flux_threshold" ->
            ShieldTargetingSpec(ShieldTargetingKind.TARGET_SHIELD, threshold ?: targetShieldDefaultPercent())
        else -> null
    }
}

private fun shieldSemanticIncompatibilityReason(firstTag: String, secondTag: String): String? {
    val first = shieldTargetingSpec(firstTag) ?: return null
    val second = shieldTargetingSpec(secondTag) ?: return null
    if (
        first.kind == ShieldTargetingKind.NO_SHIELD && second.kind == ShieldTargetingKind.TARGET_SHIELD ||
        second.kind == ShieldTargetingKind.NO_SHIELD && first.kind == ShieldTargetingKind.TARGET_SHIELD
    ) {
        return "NoShield requires targets without useful shields, while TargetShield requires a useful shield target."
    }

    val targetThreshold = when {
        first.kind == ShieldTargetingKind.TARGET_SHIELD -> first.thresholdPercent
        second.kind == ShieldTargetingKind.TARGET_SHIELD -> second.thresholdPercent
        else -> null
    }
    val avoidThreshold = when {
        first.kind == ShieldTargetingKind.AVOID_SHIELD -> first.thresholdPercent
        second.kind == ShieldTargetingKind.AVOID_SHIELD -> second.thresholdPercent
        else -> null
    }
    if (targetThreshold != null && avoidThreshold != null && targetThreshold >= avoidThreshold) {
        return "these shield thresholds do not overlap."
    }
    return null
}

private fun isTargetBigFighterConflict(firstTemplate: String, secondTemplate: String): Boolean {
    return (firstTemplate == "TargetBig" && (secondTemplate == "TargetFighter" || isHardPdOnlyTemplate(secondTemplate))) ||
        (secondTemplate == "TargetBig" && (firstTemplate == "TargetFighter" || isHardPdOnlyTemplate(firstTemplate)))
}

val tagIncompatibility = mapOf(
    "PD" to listOf("Opportunist"),
    "TargetFighter" to listOf("NoFighter", "Opportunist"),
    "NoFighter" to listOf("TargetFighter"),
    "Opportunist" to listOf("TargetFighter", "PD"),
    "SyncWindow" to listOf("SyncVolley", "Ambush"),
    "SyncVolley" to listOf("SyncWindow", "Ambush"),
    "Ambush" to listOf("SyncWindow", "SyncVolley")
)

fun isIncompatibleWithExistingTags(tag: String, existingTags: List<String>): Boolean {
    return blockedBySelectedTags(tag, existingTags) != null
}

fun tagIncompatibilityReason(tag: String, existingTags: List<String>): String? {
    return blockedBySelectedTags(tag, existingTags)
}

fun blockedBySelectedTags(candidate: String, selectedTags: Collection<String>): String? {
    firstIncompatibleExistingTag(candidate, selectedTags)?.let { existingTag ->
        return pairwiseIncompatibilityReason(candidate, existingTag)
    }
    return selectedTagSetBlockReason(candidate, selectedTags)
}

fun tagUnavailableReasonForWeaponGroup(
    groupIndex: Int,
    sh: FleetMemberAPI,
    tag: String,
    currentTags: List<String>,
): String? {
    tagDisabledReasonForGroup(groupIndex, sh, tag)?.let { return it }
    val otherTags = currentTags.toMutableList().apply { remove(tag) }
    return tagIncompatibilityReason(tag, otherTags)
}

private fun firstIncompatibleExistingTag(tag: String, existingTags: Collection<String>): String? {
    for (existingTag in existingTags) {
        if (isIncompatibleWithExistingTag(tag, existingTag)) return existingTag
    }
    return null
}

private fun pairwiseIncompatibilityReason(tag: String, existingTag: String): String {
    shieldSemanticIncompatibilityReason(tag, existingTag)?.let { return it }
    val tagTemplate = tagNameToRegexName(tag)
    val existingTemplate = tagNameToRegexName(existingTag)
    if (
        (isTargetShieldTemplate(tagTemplate) && existingTemplate == "TargetOverloaded") ||
        (tagTemplate == "TargetOverloaded" && isTargetShieldTemplate(existingTemplate))
    ) {
        return "TargetOverloaded selects defenseless ships, while TargetShield requires a useful shield target."
    }
    return "it cannot be combined with $existingTag."
}

private fun selectedTagSetBlockReason(candidate: String, selectedTags: Collection<String>): String? {
    val selectedTemplates = (selectedTags + candidate).map(::tagNameToRegexName).toSet()
    val hasPdOnly = selectedTemplates.any(::isHardPdOnlyTemplate)
    val hasNoFighter = "NoFighter" in selectedTemplates
    val hasNoMissile = "NoMissile" in selectedTemplates
    val hasNoPd = "NoPD" in selectedTemplates
    val hasTargetShield = selectedTemplates.any(::isTargetShieldTemplate)

    if (hasPdOnly && hasNoFighter && hasNoMissile) {
        return "PD with NoFighter and NoMissile has no valid targets."
    }
    if (hasPdOnly && hasNoFighter && hasNoPd) {
        return "PD and NoPD leave only fighters, and NoFighter removes them."
    }
    if (hasPdOnly && hasNoFighter && hasTargetShield) {
        return "PD + NoFighter leaves missiles, but TargetShield can only fire at ships."
    }

    return null
}

private fun isIncompatibleWithExistingTag(tag: String, existingTag: String): Boolean =
    WeaponTagNameCache.incompatiblePair(tag, existingTag) {
        if (EditableWeaponTagDefinitions.areMutuallyExclusive(tag, existingTag)) return@incompatiblePair true
        if (EditableWeaponTagDefinitions.sharesEditableDefinition(tag, existingTag)) return@incompatiblePair false
        val modTag = tagNameToRegexName(tag)
        val existingModTag = tagNameToRegexName(existingTag)
        if (isTargetBigFighterConflict(modTag, existingModTag)) return@incompatiblePair true
        if (shieldSemanticIncompatibilityReason(tag, existingTag) != null) return@incompatiblePair true
        if (
            (isTargetShieldTemplate(modTag) && existingModTag == "TargetOverloaded") ||
            (modTag == "TargetOverloaded" && isTargetShieldTemplate(existingModTag))
        ) {
            return@incompatiblePair true
        }
        tagIncompatibility[modTag]?.contains(existingModTag) == true
    }
