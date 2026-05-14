package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions
import kotlin.math.roundToInt

private val holdTotalFluxWithCapCleanupRegex = Regex("HoldFire\\(TF>(\\d+)%,TF<(\\d+)%\\)")
private val holdHardFluxWithCapCleanupRegex = Regex("HoldFire\\(HF>(\\d+)%,TF<(\\d+)%\\)")

private data class ThresholdCanonicalizationRule(
    val canonicalRegex: Regex,
    val legacyRegex: Regex,
    val canonicalPrefix: String,
    val invertLegacyThreshold: Boolean = false
)

private val thresholdCanonicalizationRules = listOf(
    ThresholdCanonicalizationRule(forceFireTotalFluxRegex, forceFireTotalFluxLegacyRegex, "Force(TF<"),
    ThresholdCanonicalizationRule(forceFireSoftFluxRegex, forceFireSoftFluxLegacyRegex, "Force(SF<"),
    ThresholdCanonicalizationRule(avoidShieldTotalFluxRegex, avoidShieldTotalFluxLegacyRegex, "AvoidShield(TF>", invertLegacyThreshold = true),
    ThresholdCanonicalizationRule(avoidShieldSoftFluxRegex, avoidShieldSoftFluxLegacyRegex, "AvoidShield(SF>", invertLegacyThreshold = true),
    ThresholdCanonicalizationRule(targetShieldTotalFluxRegex, targetShieldTotalFluxLegacyRegex, "TargetShield(TF>", invertLegacyThreshold = true),
    ThresholdCanonicalizationRule(targetShieldSoftFluxRegex, targetShieldSoftFluxLegacyRegex, "TargetShield(SF>", invertLegacyThreshold = true),
    ThresholdCanonicalizationRule(pdSoftFluxRegex, burstPDSoftFluxLegacyRegex, "PD(SF>", invertLegacyThreshold = true),
    ThresholdCanonicalizationRule(pdTotalFluxRegex, pdTotalFluxLegacyRegex, "PD(TF>")
)

private fun canonicalizeAvoidArmorTag(tag: String): String? {
    val match = avoidArmorRegex.matchEntire(tag) ?: return null
    val damageTypeSuffix = DamageTypeExclusions.fromTokens(match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() })
        ?.suffix()
        .orEmpty()
    return "AvoidArmor(${match.groupValues[1]}%$damageTypeSuffix)"
}

private fun canonicalInvertThreshold(threshold: Float): Float = (1f - threshold).coerceIn(0f, 1f)

private fun canonicalThresholdAsPercent(threshold: Float): Int = (threshold.coerceIn(0f, 1f) * 100f).roundToInt()

private fun invertedRegexThresholdAsPercentageString(regex: Regex, name: String): String {
    return "${canonicalThresholdAsPercent(canonicalInvertThreshold(extractPercentThresholdFraction(regex, name)))}%"
}

private fun settingAsActivationThreshold(settingValue: Float): String {
    return "${canonicalThresholdAsPercent(canonicalInvertThreshold(settingValue))}%"
}

private fun compactAliasRegex(pattern: String): Regex = Regex(pattern, RegexOption.IGNORE_CASE)

private val compactTagAliasReplacements = listOf(
    compactAliasRegex("total\\s*(?:flux|flx)") to "TF",
    compactAliasRegex("soft\\s*(?:flux|flx)") to "SF",
    compactAliasRegex("hard\\s*(?:flux|flx)") to "HF",
    compactAliasRegex("(?<=\\()(?:flux|flx)(?=[<>])") to "TF",
    compactAliasRegex("avoid|avd|no") to "Avoid",
    compactAliasRegex("target|tgt") to "Target",
    compactAliasRegex("missiles|mssles|missile|mssle") to "Missile",
    compactAliasRegex("fighters|fghtrs|fighter|fghtr") to "Fighter",
    compactAliasRegex("waste") to "Waste",
    compactAliasRegex("overloaded|overload") to "Overloaded",
    compactAliasRegex("phased|phase") to "Phase",
    compactAliasRegex("wounded|wound") to "Wounded",
    compactAliasRegex("healthy|health") to "Healthy",
    compactAliasRegex("force|frce") to "Force",
    compactAliasRegex("autofire|af") to "AutoFire",
    compactAliasRegex("shields|shield|shld") to "Shield",
    compactAliasRegex("armoured|armored|armour|armor|armr") to "Armor",
    compactAliasRegex("ships|ship|shp") to "Ship",
)

private fun normalizeCompactTagAliases(tag: String): String {
    var normalized = tag.trim().replace(Regex("\\s+"), "")
    compactTagAliasReplacements.forEach { (regex, replacement) ->
        normalized = regex.replace(normalized, replacement)
    }
    normalized = Regex("AvoidPD\\(W>", RegexOption.IGNORE_CASE).replace(normalized, "AvoidPD(Waste>")
    return normalized
}

fun clearWeaponTagNameCaches() {
    WeaponTagNameCache.clear()
}

private val canonicalSimpleWeaponTags = setOf(
    "PD",
    "PrioSmall",
    "PrioBig",
    "NoPD",
    "TargetFighter",
    "AvoidShield",
    "TargetShield",
    "AvoidShield+",
    "TargetShield+",
    "NoFighter",
    "ConserveAmmo",
    "ConservePDAmmo",
    "Opportunist",
    "AvoidDebris",
    "TargetBig",
    "TargetSmall",
    "ForceAutoFire",
    "DoNotShoot",
    "AvoidPhased",
    "TargetPhase",
    "NoShield",
    "NoMissile",
    "TargetOverloaded",
    "LowRoF",
    "Burst",
    "BurstCancel",
    "SyncWindow",
    "SyncVolley",
    "Ambush",
    "DisableTags",
    "PrioFighter",
    "PrioMissile",
    "PrioShip",
    "PrioWounded",
    "PrioWoundedPD",
    "PrioHealthy",
    "PrioFocused",
    "PrioShields",
    "PrioHull",
    "PrioClose",
    "PrioFar",
    "BlockBeams",
    "CustomAI",
    "PrioDense",
)

private fun canonicalizeThresholdTag(
    inputTag: String,
    canonicalRegex: Regex,
    legacyRegex: Regex,
    canonicalPrefix: String,
    invertLegacyThreshold: Boolean = false
): String? {
    if (canonicalRegex.matches(inputTag)) return inputTag
    if (!legacyRegex.matches(inputTag)) return null
    val threshold = if (invertLegacyThreshold) {
        invertedRegexThresholdAsPercentageString(legacyRegex, inputTag)
    } else {
        extractPercentThresholdDisplayString(legacyRegex, inputTag)
    }
    return "$canonicalPrefix$threshold)"
}

fun canonicalizeWeaponTagName(tag: String): String =
    WeaponTagNameCache.canonical(tag) { canonicalizeWeaponTagNameUncached(tag) }

private fun canonicalizeWeaponTagNameUncached(tag: String): String {
    canonicalizeKnownWeaponTagName(tag)?.let { return it }
    val normalized = normalizeCompactTagAliases(tag)
    if (normalized != tag) {
        canonicalizeKnownWeaponTagName(normalized)?.let { return it }
    }
    return tag
}

private fun canonicalizeKnownWeaponTagName(tag: String): String? {
    if (tag in canonicalSimpleWeaponTags) return tag
    when {
        holdTotalFluxRegex.matches(tag) -> return tag
        holdTotalFluxWithCapCleanupRegex.matches(tag) -> return "HoldFire(TF>${extractPercentThresholdDisplayString(holdTotalFluxWithCapCleanupRegex, tag)})"
        holdTotalFluxAliasRegex.matches(tag) -> return "HoldFire(TF>${extractPercentThresholdDisplayString(holdTotalFluxAliasRegex, tag)})"
        holdTotalFluxLegacyRegex.matches(tag) -> return "HoldFire(TF>${extractPercentThresholdDisplayString(holdTotalFluxLegacyRegex, tag)})"
        holdSoftFluxRegex.matches(tag) -> return tag
        holdSoftFluxAliasRegex.matches(tag) -> return "HoldFire(SF>${extractPercentThresholdDisplayString(holdSoftFluxAliasRegex, tag)})"
        holdSoftFluxLegacyRegex.matches(tag) -> return "HoldFire(SF>${extractPercentThresholdDisplayString(holdSoftFluxLegacyRegex, tag)})"
        holdHardFluxRegex.matches(tag) -> return tag
        holdHardFluxWithCapCleanupRegex.matches(tag) -> return "HoldFire(HF>${extractPercentThresholdDisplayString(holdHardFluxWithCapCleanupRegex, tag)})"
        holdHardFluxAliasRegex.matches(tag) -> return "HoldFire(HF>${extractPercentThresholdDisplayString(holdHardFluxAliasRegex, tag)})"
    }

    thresholdCanonicalizationRules.forEach { rule ->
        canonicalizeThresholdTag(
            inputTag = tag,
            canonicalRegex = rule.canonicalRegex,
            legacyRegex = rule.legacyRegex,
            canonicalPrefix = rule.canonicalPrefix,
            invertLegacyThreshold = rule.invertLegacyThreshold
        )?.let { return it }
    }
    canonicalizeSyncTagOptions(tag)?.let { return it }

    return when {
        burstPDSoftFluxAliasRegex.matches(tag) -> "PD(SF>${invertedRegexThresholdAsPercentageString(burstPDSoftFluxAliasRegex, tag)})"
        pdSoftFluxRegex.matches(tag) -> tag
        pdHardFluxRegex.matches(tag) -> tag
        holdHardFluxRegex.matches(tag) -> tag
        forceFireHardFluxRegex.matches(tag) -> tag
        avoidShieldDamageExclusionRegex.matches(tag) -> tag
        avoidShieldHardFluxRegex.matches(tag) -> tag
        targetShieldDamageExclusionRegex.matches(tag) -> tag
        targetShieldHardFluxRegex.matches(tag) -> tag
        opportunistAmmoRegex.matches(tag) -> tag
        opportunistTunedRegex.matches(tag) -> tag
        conserveAmmoRegex.matches(tag) -> "Opportunist(A<${extractPercentThresholdDisplayString(conserveAmmoRegex, tag)})"
        pdAmmoRegex.matches(tag) -> tag
        conservePDAmmoRegex.matches(tag) -> "PD(A<${extractPercentThresholdDisplayString(conservePDAmmoRegex, tag)})"
        conservePDAmmoShortRegex.matches(tag) -> "PD(A<${extractPercentThresholdDisplayString(conservePDAmmoShortRegex, tag)})"
        noPdWasteRegex.matches(tag) -> tag
        noPdWasteLegacyRegex.matches(tag) -> "AvoidPD(Waste>${extractPercentThresholdDisplayString(noPdWasteLegacyRegex, tag)})"
        noPdHealthRegex.matches(tag) -> tag
        noPdHealthLegacyRegex.matches(tag) -> "AvoidPD(H<${extractRawNumericThreshold(noPdHealthLegacyRegex, tag).toInt()})"
        ignoreMinorPDRegex.matches(tag) -> "AvoidPD(H<${extractRawNumericThreshold(ignoreMinorPDRegex, tag).toInt()})"
        avoidArmorRegex.matches(tag) -> canonicalizeAvoidArmorTag(tag)
        targetPhaseDamageExclusionRegex.matches(tag) -> tag
        avoidPhasedDamageExclusionRegex.matches(tag) -> tag
        prioWoundedDamageExclusionRegex.matches(tag) -> tag
        prioSmallRegex.matches(tag) -> tag
        prioSmallLegacyRegex.matches(tag) -> "PrioSmall(${extractRawNumericThreshold(prioSmallLegacyRegex, tag).toInt()})"
        prioFighterRegex.matches(tag) -> tag
        prioMissileRegex.matches(tag) -> tag
        prioShipRegex.matches(tag) -> tag
        prioShipsRegex.matches(tag) -> "PrioShip(${extractRawNumericThreshold(prioShipsRegex, tag).toInt()})"
        prioFocusedRegex.matches(tag) -> tag
        prioShieldsDamageExclusionRegex.matches(tag) -> tag
        prioShieldsRegex.matches(tag) -> tag
        prioHullDamageExclusionRegex.matches(tag) -> tag
        prioHullRegex.matches(tag) -> tag
        prioCloseRegex.matches(tag) -> tag
        prioFarRegex.matches(tag) -> tag
        tag == "ForceAF" -> "ForceAutoFire"
        tag == "PrioPD" -> "PrioSmall"
        tag == "PrioritisePD" -> "PrioSmall"
        tag == "PrioritizePD" -> "PrioSmall"
        tag == "CnsrvPDAmmo" -> "ConservePDAmmo"
        tag == "Fighter" -> "TargetFighter"
        tag == "Overloaded" -> "TargetOverloaded"
        tag == "AvoidFighter" -> "NoFighter"
        tag == "NoFighters" -> "NoFighter"
        tag == "AvoidMissile" -> "NoMissile"
        tag == "NoMissiles" -> "NoMissile"
        tag == "BigShip" -> "TargetBig"
        tag == "BigShips" -> "TargetBig"
        tag == "SmallShip" -> "TargetSmall"
        tag == "SmallShips" -> "TargetSmall"
        tag == "PrioShips" -> "PrioShip"
        tag == "AvoidShields" -> "AvoidShield"
        tag == "TargetShields" -> "TargetShield"
        tag == "AvdShields+" -> "AvoidShield+"
        tag == "TgtShields+" -> "TargetShield+"
        tag == "AvdShieldsFT" -> "AvoidShield(TF>${settingAsActivationThreshold(Settings.avoidShieldAtTotalFlux())})"
        tag == "TgtShieldsFT" -> "TargetShield(TF>${settingAsActivationThreshold(Settings.targetShieldAtTotalFlux())})"
        tag == "ShieldOff" -> "NoShield"
        tag == "ShieldsOff" -> "NoShield"
        tag == "IgnoreMinorPD" -> "AvoidPD(H<145)"
        tag == "AvoidPD" -> "NoPD"
        tag == "AvoidPhase" -> "AvoidPhased"
        tag == "TargetPhased" -> "TargetPhase"
        else -> null
    }
}

fun canonicalizeWeaponTagNames(tags: List<String>): List<String> = tags.map { canonicalizeWeaponTagName(it) }.distinct()
