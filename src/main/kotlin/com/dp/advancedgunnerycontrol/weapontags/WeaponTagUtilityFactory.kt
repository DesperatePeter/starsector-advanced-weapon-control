package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidArmorTag
import com.dp.advancedgunnerycontrol.weaponais.tags.AvoidPhaseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.BurstPDSoftFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ConserveAmmoTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ConservePDAmmoTag
import com.dp.advancedgunnerycontrol.weaponais.tags.IgnoreMinorPDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.NoPDWasteTag
import com.dp.advancedgunnerycontrol.weaponais.tags.OpportunistTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PDAtHardFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PDAtTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PanicFireTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeWoundedTag
import com.dp.advancedgunnerycontrol.weaponais.tags.RangeTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ReduceRoFTag
import com.dp.advancedgunnerycontrol.weaponais.tags.TargetPhaseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagUtilityFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when {
            pdSoftFluxRegex.matches(canonicalName) -> BurstPDSoftFluxTag(
                weapon,
                extractPercentThresholdFraction(pdSoftFluxRegex, canonicalName)
            )

            pdTotalFluxRegex.matches(canonicalName) -> PDAtTotalFluxTag(
                weapon,
                extractPercentThresholdFraction(pdTotalFluxRegex, canonicalName)
            )

            pdHardFluxRegex.matches(canonicalName) -> PDAtHardFluxTag(
                weapon,
                extractPercentThresholdFraction(pdHardFluxRegex, canonicalName)
            )

            opportunistAmmoRegex.matches(canonicalName) -> {
                val match = opportunistAmmoRegex.matchEntire(canonicalName) ?: return null
                ConserveAmmoTag(
                    weapon,
                    ammoThresholdOverride = match.groupValues[1].toFloat() / 100f,
                    kineticThresholdOverride = match.groupValues.getOrNull(2)
                        ?.takeIf { it.isNotBlank() }
                        ?.toFloatOrNull()
                        ?.let { it / 100f },
                    highExplosiveThresholdOverride = match.groupValues.getOrNull(3)
                        ?.takeIf { it.isNotBlank() }
                        ?.toFloatOrNull()
                        ?.let { it / 100f },
                    triggerHappinessModifierOverride = match.groupValues.getOrNull(4)
                        ?.takeIf { it.isNotBlank() }
                        ?.toFloatOrNull()
                        ?.let { it / 100f },
                )
            }

            opportunistTunedRegex.matches(canonicalName) -> {
                val match = opportunistTunedRegex.matchEntire(canonicalName) ?: return null
                OpportunistTag(
                    weapon,
                    kineticThresholdOverride = match.groupValues[1].toFloat() / 100f,
                    highExplosiveThresholdOverride = match.groupValues[2].toFloat() / 100f,
                    triggerHappinessModifierOverride = match.groupValues[3].toFloat() / 100f,
                )
            }

            pdAmmoRegex.matches(canonicalName) -> ConservePDAmmoTag(
                weapon,
                extractPercentThresholdFraction(pdAmmoRegex, canonicalName)
            )

            noPdWasteRegex.matches(canonicalName) -> NoPDWasteTag(
                weapon,
                extractPercentThresholdFraction(noPdWasteRegex, canonicalName),
                cleanupDamageCapOverride = extractOptionalRawRegexSecondValue(noPdWasteRegex, canonicalName),
            )

            noPdHealthRegex.matches(canonicalName) -> IgnoreMinorPDTag(
                weapon,
                extractRawNumericThreshold(noPdHealthRegex, canonicalName)
            )

            avoidArmorRegex.matches(canonicalName) -> AvoidArmorTag(
                weapon,
                extractPercentThresholdFraction(avoidArmorRegex, canonicalName),
                damageTypeExclusions = extractOptionalThresholdDamageTypeExclusions(avoidArmorRegex, canonicalName)
            )

            targetPhaseDamageExclusionRegex.matches(canonicalName) -> TargetPhaseTag(
                weapon,
                extractSimpleDamageTypeExclusions(targetPhaseDamageExclusionRegex, canonicalName)
            )

            avoidPhasedDamageExclusionRegex.matches(canonicalName) -> AvoidPhaseTag(
                weapon,
                extractSimpleDamageTypeExclusions(avoidPhasedDamageExclusionRegex, canonicalName)
            )

            prioWoundedDamageExclusionRegex.matches(canonicalName) -> PrioritizeWoundedTag(
                weapon,
                extractSimpleDamageTypeExclusions(prioWoundedDamageExclusionRegex, canonicalName)
            )

            panicFireRegex.matches(canonicalName) -> PanicFireTag(
                weapon,
                extractPercentThresholdFraction(panicFireRegex, canonicalName)
            )

            rangeRegex.matches(canonicalName) -> RangeTag(
                weapon,
                extractPercentThresholdFraction(rangeRegex, canonicalName)
            )

            rofRegex.matches(canonicalName) -> ReduceRoFTag(
                weapon,
                extractPercentThresholdFraction(rofRegex, canonicalName)
            )

            else -> null
        }
    }
}
