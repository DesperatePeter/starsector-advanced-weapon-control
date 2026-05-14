package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.ForceFireHardFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ForceFireSoftFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.ForceFireTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.HoldHardFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.HoldSoftFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.HoldTotalFluxTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagFluxFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when {
            holdTotalFluxRegex.matches(canonicalName) -> HoldTotalFluxTag(
                weapon,
                extractPercentThresholdFraction(holdTotalFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(holdTotalFluxRegex, canonicalName)
            )

            holdSoftFluxRegex.matches(canonicalName) -> HoldSoftFluxTag(
                weapon,
                extractPercentThresholdFraction(holdSoftFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(holdSoftFluxRegex, canonicalName),
                extractOptionalHoldSoftFluxBeamWindow(canonicalName)
            )

            holdHardFluxRegex.matches(canonicalName) -> HoldHardFluxTag(
                weapon,
                extractPercentThresholdFraction(holdHardFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(holdHardFluxRegex, canonicalName)
            )

            forceFireTotalFluxRegex.matches(canonicalName) -> ForceFireTotalFluxTag(
                weapon,
                extractPercentThresholdFraction(forceFireTotalFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(forceFireTotalFluxRegex, canonicalName)
            )

            forceFireSoftFluxRegex.matches(canonicalName) -> ForceFireSoftFluxTag(
                weapon,
                extractPercentThresholdFraction(forceFireSoftFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(forceFireSoftFluxRegex, canonicalName)
            )

            forceFireHardFluxRegex.matches(canonicalName) -> ForceFireHardFluxTag(
                weapon,
                extractPercentThresholdFraction(forceFireHardFluxRegex, canonicalName),
                extractOptionalTotalFluxCapFraction(forceFireHardFluxRegex, canonicalName)
            )

            else -> null
        }
    }
}
