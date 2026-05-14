package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldHardFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldSoftFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidShieldTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldHardFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldSoftFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetShieldTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagShieldFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when {
            avoidShieldDamageExclusionRegex.matches(canonicalName) -> AvoidShieldTag(
                weapon,
                damageTypeExclusions = extractSimpleDamageTypeExclusions(avoidShieldDamageExclusionRegex, canonicalName)
            )

            avoidShieldThresholdRegex.matches(canonicalName) -> AvoidShieldTag(
                weapon,
                extractPercentThresholdFraction(avoidShieldThresholdRegex, canonicalName),
                damageTypeExclusions = extractOptionalThresholdDamageTypeExclusions(avoidShieldThresholdRegex, canonicalName)
            )

            avoidShieldTotalFluxRegex.matches(canonicalName) -> AvoidShieldTotalFluxTag(
                weapon,
                extractPercentThresholdFraction(avoidShieldTotalFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(avoidShieldTotalFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(avoidShieldTotalFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(avoidShieldTotalFluxRegex, canonicalName)
            )

            avoidShieldSoftFluxRegex.matches(canonicalName) -> AvoidShieldSoftFluxTag(
                weapon,
                extractPercentThresholdFraction(avoidShieldSoftFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(avoidShieldSoftFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(avoidShieldSoftFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(avoidShieldSoftFluxRegex, canonicalName)
            )

            avoidShieldHardFluxRegex.matches(canonicalName) -> AvoidShieldHardFluxTag(
                weapon,
                extractPercentThresholdFraction(avoidShieldHardFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(avoidShieldHardFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(avoidShieldHardFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(avoidShieldHardFluxRegex, canonicalName)
            )

            targetShieldDamageExclusionRegex.matches(canonicalName) -> TargetShieldTag(
                weapon,
                damageTypeExclusions = extractSimpleDamageTypeExclusions(targetShieldDamageExclusionRegex, canonicalName)
            )

            targetShieldThresholdRegex.matches(canonicalName) -> TargetShieldTag(
                weapon,
                extractPercentThresholdFraction(targetShieldThresholdRegex, canonicalName),
                damageTypeExclusions = extractOptionalThresholdDamageTypeExclusions(targetShieldThresholdRegex, canonicalName)
            )

            targetShieldTotalFluxRegex.matches(canonicalName) -> TargetShieldTotalFluxTag(
                weapon,
                extractPercentThresholdFraction(targetShieldTotalFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(targetShieldTotalFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(targetShieldTotalFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(targetShieldTotalFluxRegex, canonicalName)
            )

            targetShieldSoftFluxRegex.matches(canonicalName) -> TargetShieldSoftFluxTag(
                weapon,
                extractPercentThresholdFraction(targetShieldSoftFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(targetShieldSoftFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(targetShieldSoftFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(targetShieldSoftFluxRegex, canonicalName)
            )

            targetShieldHardFluxRegex.matches(canonicalName) -> TargetShieldHardFluxTag(
                weapon,
                extractPercentThresholdFraction(targetShieldHardFluxRegex, canonicalName),
                shieldThresholdOverride = extractOptionalShieldThreshold(targetShieldHardFluxRegex, canonicalName),
                totalFluxCap = extractOptionalShieldTagTotalFluxCapFraction(targetShieldHardFluxRegex, canonicalName),
                damageTypeExclusions = extractOptionalShieldDamageTypeExclusions(targetShieldHardFluxRegex, canonicalName)
            )

            else -> null
        }
    }
}
