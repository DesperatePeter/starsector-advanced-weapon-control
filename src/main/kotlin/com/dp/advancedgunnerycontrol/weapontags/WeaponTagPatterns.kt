package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.DamageTypeExclusions

// NOTE: Tag names should NOT exceed 13 characters to be able to cleanly fit on buttons!

private const val LEGACY_TOTAL_FLUX_TOKEN = "Fl?u?x?"
private const val OPTIONAL_TOTAL_FLUX_CAP_PATTERN = "(?:,TF<(\\d+)%)?"
private const val OPTIONAL_HOLD_SOFT_FLUX_BEAM_WINDOW_PATTERN = "(?:,Beam<(\\d+(?:\\.\\d+)?)s)?"
private const val OPTIONAL_AVOID_SHIELD_THRESHOLD_PATTERN = "(?:,S<(\\d+)%)?"
private const val OPTIONAL_TARGET_SHIELD_THRESHOLD_PATTERN = "(?:,S>(\\d+)%)?"
private const val DAMAGE_TYPE_EXCLUSION_LIST_PATTERN = "(?:K|HE|F|E|B|M|P)(?:,(?:K|HE|F|E|B|M|P))*"
private const val OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN = "(?:,Ignore<($DAMAGE_TYPE_EXCLUSION_LIST_PATTERN)>)?"
private const val SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN = "\\(Ignore<($DAMAGE_TYPE_EXCLUSION_LIST_PATTERN)>\\)"

val holdTotalFluxRegex = Regex("HoldFire\\(TF>(\\d+)%\\)")
val holdTotalFluxAliasRegex = Regex("Hold\\(TF>(\\d+)%\\)")
val holdTotalFluxLegacyRegex = Regex("Hold(?:FT)?\\($LEGACY_TOTAL_FLUX_TOKEN>(\\d+)%\\)")
val holdSoftFluxRegex = Regex("HoldFire\\(SF>(\\d+)%$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_HOLD_SOFT_FLUX_BEAM_WINDOW_PATTERN\\)")
val holdSoftFluxAliasRegex = Regex("Hold\\(SF>(\\d+)%\\)")
val holdHardFluxRegex = Regex("HoldFire\\(HF>(\\d+)%\\)")
val holdHardFluxAliasRegex = Regex("Hold\\(HF>(\\d+)%\\)")
val holdSoftFluxLegacyRegex = Regex("HoldSFT\\($LEGACY_TOTAL_FLUX_TOKEN>(\\d+)%\\)")
val forceFireTotalFluxRegex = Regex("Force\\(TF<(\\d+)%$OPTIONAL_TOTAL_FLUX_CAP_PATTERN\\)")
val forceFireTotalFluxLegacyRegex = Regex("Force(?:F|FT)\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val forceFireSoftFluxRegex = Regex("Force\\(SF<(\\d+)%$OPTIONAL_TOTAL_FLUX_CAP_PATTERN\\)")
val forceFireHardFluxRegex = Regex("Force\\(HF<(\\d+)%$OPTIONAL_TOTAL_FLUX_CAP_PATTERN\\)")
val forceFireSoftFluxLegacyRegex = Regex("ForceSFT\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val avoidShieldDamageExclusionRegex = Regex("AvoidShield$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val avoidShieldThresholdRegex = Regex("AvoidShield\\(S<(\\d+)%$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val avoidShieldTotalFluxRegex = Regex("AvoidShield\\(TF>(\\d+)%$OPTIONAL_AVOID_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val avoidShieldSoftFluxRegex = Regex("AvoidShield\\(SF>(\\d+)%$OPTIONAL_AVOID_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val avoidShieldHardFluxRegex = Regex("AvoidShield\\(HF>(\\d+)%$OPTIONAL_AVOID_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val avoidShieldTotalFluxLegacyRegex = Regex("(?:AvShldFT|AvdShieldsFT)\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val avoidShieldSoftFluxLegacyRegex = Regex("(?:AvShldSFT|AvShlddSFT|AvdShieldsSFT)\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val targetShieldDamageExclusionRegex = Regex("TargetShield$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val targetShieldThresholdRegex = Regex("TargetShield\\(S>(\\d+)%$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val targetShieldTotalFluxRegex = Regex("TargetShield\\(TF>(\\d+)%$OPTIONAL_TARGET_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val targetShieldSoftFluxRegex = Regex("TargetShield\\(SF>(\\d+)%$OPTIONAL_TARGET_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val targetShieldHardFluxRegex = Regex("TargetShield\\(HF>(\\d+)%$OPTIONAL_TARGET_SHIELD_THRESHOLD_PATTERN$OPTIONAL_TOTAL_FLUX_CAP_PATTERN$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val targetShieldTotalFluxLegacyRegex = Regex("(?:TgtShldFT|TgtShieldsFT)\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val targetShieldSoftFluxLegacyRegex = Regex("(?:TgtShldSFT|TgtShieldsSFT)\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val pdSoftFluxRegex = Regex("PD\\(SF>(\\d+)%\\)")
val burstPDSoftFluxAliasRegex = Regex("BurstPD\\(SF>(\\d+)%\\)")
val burstPDSoftFluxLegacyRegex = Regex("BurstPDSFT\\($LEGACY_TOTAL_FLUX_TOKEN<(\\d+)%\\)")
val pdTotalFluxRegex = Regex("PD\\(TF>(\\d+)%\\)")
val pdHardFluxRegex = Regex("PD\\(HF>(\\d+)%\\)")
val pdTotalFluxLegacyRegex = Regex("PD\\($LEGACY_TOTAL_FLUX_TOKEN>(\\d+)%\\)")
val opportunistAmmoRegex = Regex("Opportunist\\(A<(\\d+)%(?:,K<(\\d+)%,HE<(\\d+)%,TH<(\\d+)%)?\\)")
val opportunistTunedRegex = Regex("Opportunist\\(K<(\\d+)%,HE<(\\d+)%,TH<(\\d+)%\\)")
val conserveAmmoRegex = Regex("ConserveAmmo\\(A<(\\d+)%\\)")
val pdAmmoRegex = Regex("PD\\(A<(\\d+)%\\)")
val conservePDAmmoRegex = Regex("ConservePDAmmo\\(A<(\\d+)%\\)")
val conservePDAmmoShortRegex = Regex("CnsrvPDAmmo\\(A<(\\d+)%\\)")
val noPdWasteRegex = Regex("AvoidPD\\(Waste>(\\d+)%(?:,Cap<(\\d+))?\\)")
val noPdHealthRegex = Regex("AvoidPD\\(H<(\\d+)\\)")
val noPdWasteLegacyRegex = Regex("NoPD\\(Waste>(\\d+)%\\)")
val noPdHealthLegacyRegex = Regex("NoPD\\(H<(\\d+)\\)")
val ignoreMinorPDRegex = Regex("IgnoreMinorPD\\(H<(\\d+)\\)")
val avoidArmorRegex = Regex("(?:AvoidArmor|AvdArmor)\\((\\d+)%$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val panicFireRegex = Regex("Panic\\(H<(\\d+)%\\)")
val rangeRegex = Regex("Range<(\\d+)%")
val rofRegex = Regex("LowRoF\\((\\d+)%\\)")

//val prioPdRegex = Regex("PrioP[dD]\\((\\d+)\\)")
//val prioFightersRegex = Regex("PrioFighter\\((\\d+)\\)")
//val prioMissilesRegex = Regex("PrioMissile\\((\\d+)\\)")
val prioSmallRegex = Regex("PrioSmall\\((\\d+)\\)")
val prioSmallLegacyRegex = Regex("(?:PrioPD|PrioritizePD|PrioritisePD)\\((\\d+)\\)")
val prioFighterRegex = Regex("PrioFighter\\((\\d+)\\)")
val prioMissileRegex = Regex("PrioMissile\\((\\d+)\\)")
val prioShipRegex = Regex("PrioShip\\((\\d+)\\)")
val prioShipsRegex = Regex("PrioShips\\((\\d+)\\)")
val prioFocusedRegex = Regex("PrioFocused\\((\\d+)\\)")
val prioWoundedDamageExclusionRegex = Regex("PrioWounded$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val targetPhaseDamageExclusionRegex = Regex("TargetPhase$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val avoidPhasedDamageExclusionRegex = Regex("AvoidPhased$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val prioShieldsDamageExclusionRegex = Regex("PrioShields$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val prioShieldsRegex = Regex("PrioShields\\((\\d+)$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val prioHullDamageExclusionRegex = Regex("PrioHull$SIMPLE_DAMAGE_TYPE_EXCLUSION_PATTERN")
val prioHullRegex = Regex("PrioHull\\((\\d+)$OPTIONAL_DAMAGE_TYPE_EXCLUSION_PATTERN\\)")
val prioCloseRegex = Regex("PrioClose\\((\\d+)\\)")
val prioFarRegex = Regex("PrioFar\\((\\d+)\\)")

fun extractPercentThresholdFraction(regex: Regex, name: String): Float {
    return (regex.matchEntire(name)?.groupValues?.get(1)?.toFloat() ?: 0f) / 100f
}

internal fun extractOptionalTotalFluxCapFraction(regex: Regex, name: String): Float? {
    val match = regex.matchEntire(name) ?: return null
    return match.groupValues.getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
        ?.let { it / 100f }
}

internal fun extractOptionalHoldSoftFluxBeamWindow(name: String): Float? {
    val match = holdSoftFluxRegex.matchEntire(name) ?: return null
    return match.groupValues.getOrNull(3)
        ?.takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
}

internal fun extractOptionalShieldTagTotalFluxCapFraction(regex: Regex, name: String): Float? {
    val match = regex.matchEntire(name) ?: return null
    return match.groupValues.getOrNull(3)
        ?.takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
        ?.let { it / 100f }
}

internal fun extractOptionalShieldThreshold(regex: Regex, name: String): Float? {
    val match = regex.matchEntire(name) ?: return null
    return match.groupValues.getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
        ?.let { it / 100f }
}

internal fun extractOptionalThresholdDamageTypeExclusions(regex: Regex, name: String): DamageTypeExclusions {
    val match = regex.matchEntire(name) ?: return DamageTypeExclusions.NONE
    return DamageTypeExclusions.fromTokens(match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() })
        ?: DamageTypeExclusions.NONE
}

internal fun extractOptionalShieldDamageTypeExclusions(regex: Regex, name: String): DamageTypeExclusions {
    val match = regex.matchEntire(name) ?: return DamageTypeExclusions.NONE
    return DamageTypeExclusions.fromTokens(match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() })
        ?: DamageTypeExclusions.NONE
}

internal fun extractSimpleDamageTypeExclusions(regex: Regex, name: String): DamageTypeExclusions {
    val match = regex.matchEntire(name) ?: return DamageTypeExclusions.NONE
    return DamageTypeExclusions.fromTokens(match.groupValues.getOrNull(1)?.takeIf { it.isNotBlank() })
        ?: DamageTypeExclusions.NONE
}

internal fun extractPriorityDamageTypeExclusions(regex: Regex, name: String): DamageTypeExclusions {
    val match = regex.matchEntire(name) ?: return DamageTypeExclusions.NONE
    return DamageTypeExclusions.fromTokens(match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() })
        ?: DamageTypeExclusions.NONE
}

internal fun extractRawNumericThreshold(regex: Regex, name: String): Float {
    return regex.matchEntire(name)?.groupValues?.get(1)?.toFloat() ?: 0f
}

internal fun extractOptionalRawRegexSecondValue(regex: Regex, name: String): Float? {
    return regex.matchEntire(name)?.groupValues?.getOrNull(2)
        ?.takeIf { it.isNotBlank() }
        ?.toFloatOrNull()
}

fun extractPercentThresholdDisplayString(regex: Regex, name: String): String {
    return "${(extractPercentThresholdFraction(regex, name) * 100f).toInt()}%"
}
