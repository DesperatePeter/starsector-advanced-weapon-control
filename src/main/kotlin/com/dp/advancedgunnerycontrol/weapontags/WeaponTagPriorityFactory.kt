package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeCloseTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFarTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFightersTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeFocusedTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeHullTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeMissilesTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizePDTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeShieldsTag
import com.dp.advancedgunnerycontrol.weaponais.tags.PrioritizeShipTag
import com.dp.advancedgunnerycontrol.weaponais.tags.WeaponAITagBase
import com.fs.starfarer.api.combat.WeaponAPI

internal object WeaponTagPriorityFactory {
    fun create(canonicalName: String, weapon: WeaponAPI): WeaponAITagBase? {
        return when {
            prioSmallRegex.matches(canonicalName) -> PrioritizePDTag(
                weapon,
                extractRawNumericThreshold(prioSmallRegex, canonicalName)
            )

            prioFighterRegex.matches(canonicalName) -> PrioritizeFightersTag(
                weapon,
                extractRawNumericThreshold(prioFighterRegex, canonicalName)
            )

            prioMissileRegex.matches(canonicalName) -> PrioritizeMissilesTag(
                weapon,
                extractRawNumericThreshold(prioMissileRegex, canonicalName)
            )

            prioShipRegex.matches(canonicalName) -> PrioritizeShipTag(
                weapon,
                extractRawNumericThreshold(prioShipRegex, canonicalName)
            )

            prioFocusedRegex.matches(canonicalName) -> PrioritizeFocusedTag(
                weapon,
                extractRawNumericThreshold(prioFocusedRegex, canonicalName)
            )

            prioShieldsDamageExclusionRegex.matches(canonicalName) -> PrioritizeShieldsTag(
                weapon,
                damageTypeExclusions = extractSimpleDamageTypeExclusions(prioShieldsDamageExclusionRegex, canonicalName)
            )

            prioShieldsRegex.matches(canonicalName) -> PrioritizeShieldsTag(
                weapon,
                extractRawNumericThreshold(prioShieldsRegex, canonicalName),
                extractPriorityDamageTypeExclusions(prioShieldsRegex, canonicalName)
            )

            prioHullDamageExclusionRegex.matches(canonicalName) -> PrioritizeHullTag(
                weapon,
                damageTypeExclusions = extractSimpleDamageTypeExclusions(prioHullDamageExclusionRegex, canonicalName)
            )

            prioHullRegex.matches(canonicalName) -> PrioritizeHullTag(
                weapon,
                extractRawNumericThreshold(prioHullRegex, canonicalName),
                extractPriorityDamageTypeExclusions(prioHullRegex, canonicalName)
            )

            prioCloseRegex.matches(canonicalName) -> PrioritizeCloseTag(
                weapon,
                extractRawNumericThreshold(prioCloseRegex, canonicalName)
            )

            prioFarRegex.matches(canonicalName) -> PrioritizeFarTag(
                weapon,
                extractRawNumericThreshold(prioFarRegex, canonicalName)
            )

            else -> null
        }
    }
}
