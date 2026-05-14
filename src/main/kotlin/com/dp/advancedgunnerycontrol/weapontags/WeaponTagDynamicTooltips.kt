package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.combat.FluxComparator
import com.dp.advancedgunnerycontrol.combat.FluxMetric
import com.dp.advancedgunnerycontrol.settings.Settings

internal fun dynamicTagTooltip(canonicalTag: String): String {
    return when {
        prioSmallRegex.matches(canonicalTag) -> priorityMultiplierTooltip(
            "missiles, fighters, and smaller ships over larger ships",
            prioSmallRegex,
            canonicalTag
        )

        prioFighterRegex.matches(canonicalTag) -> priorityMultiplierTooltip("fighters over other targets", prioFighterRegex, canonicalTag)

        prioMissileRegex.matches(canonicalTag) -> priorityMultiplierTooltip("missiles over other targets", prioMissileRegex, canonicalTag)

        prioShipRegex.matches(canonicalTag) -> priorityMultiplierTooltip("non-fighter ships over other targets", prioShipRegex, canonicalTag)

        prioFocusedRegex.matches(canonicalTag) -> priorityMultiplierTooltip("enemy ships already targeted by allied ships", prioFocusedRegex, canonicalTag)

        prioShieldsRegex.matches(canonicalTag) -> priorityMultiplierTooltip("shielded targets over exposed-hull targets", prioShieldsRegex, canonicalTag)

        prioHullRegex.matches(canonicalTag) -> priorityMultiplierTooltip("exposed-hull targets over shielded targets", prioHullRegex, canonicalTag)

        prioCloseRegex.matches(canonicalTag) -> priorityMultiplierTooltip("nearby targets over distant targets", prioCloseRegex, canonicalTag)

        prioFarRegex.matches(canonicalTag) -> priorityMultiplierTooltip("distant targets over nearby targets", prioFarRegex, canonicalTag)

        parseSyncTagOptions(canonicalTag) != null -> {
            val options = parseSyncTagOptions(canonicalTag)!!
            baseTagTooltip(options.family) ?: "Synchronizes tagged weapons in the same weapon group."
        }

        holdTotalFluxRegex.matches(canonicalTag) -> holdFireTooltip(
            canonicalTag,
            holdTotalFluxRegex,
            FluxMetric.TOTAL,
            FluxComparator.LESS_OR_EQUAL
        )

        holdSoftFluxRegex.matches(canonicalTag) -> holdFireTooltip(
            canonicalTag,
            holdSoftFluxRegex,
            FluxMetric.SOFT,
            FluxComparator.LESS_THAN,
            requireSoftFluxCap = true
        )

        holdHardFluxRegex.matches(canonicalTag) -> holdFireTooltip(
            canonicalTag,
            holdHardFluxRegex,
            FluxMetric.HARD,
            FluxComparator.LESS_OR_EQUAL
        )

        forceFireTotalFluxRegex.matches(canonicalTag) -> forceFireTooltip(
            canonicalTag,
            forceFireTotalFluxRegex,
            FluxMetric.TOTAL,
            FluxComparator.LESS_THAN
        )

        forceFireSoftFluxRegex.matches(canonicalTag) -> forceFireTooltip(
            canonicalTag,
            forceFireSoftFluxRegex,
            FluxMetric.SOFT,
            FluxComparator.LESS_THAN,
            requireSoftFluxCap = true
        )

        forceFireHardFluxRegex.matches(canonicalTag) -> forceFireTooltip(
            canonicalTag,
            forceFireHardFluxRegex,
            FluxMetric.HARD,
            FluxComparator.LESS_THAN
        )

        avoidShieldDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("AvoidShield") ?: "No description available."

        avoidShieldThresholdRegex.matches(canonicalTag) -> "As AvoidShield, using target shield factor below ${
            extractPercentThresholdDisplayString(avoidShieldThresholdRegex, canonicalTag)
        } as the firing threshold."

        avoidShieldTotalFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("AvoidShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, avoidShieldTotalFluxRegex, FluxMetric.TOTAL, FluxComparator.GREATER_THAN, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        avoidShieldSoftFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("AvoidShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, avoidShieldSoftFluxRegex, FluxMetric.SOFT, FluxComparator.GREATER_THAN, requireSoftFluxCap = true, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        avoidShieldHardFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("AvoidShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, avoidShieldHardFluxRegex, FluxMetric.HARD, FluxComparator.GREATER_THAN, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        targetShieldDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("TargetShield") ?: "No description available."

        targetShieldThresholdRegex.matches(canonicalTag) -> "As TargetShield, using target shield factor above ${
            extractPercentThresholdDisplayString(targetShieldThresholdRegex, canonicalTag)
        } as the firing threshold."

        targetShieldTotalFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("TargetShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, targetShieldTotalFluxRegex, FluxMetric.TOTAL, FluxComparator.GREATER_THAN, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        targetShieldSoftFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("TargetShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, targetShieldSoftFluxRegex, FluxMetric.SOFT, FluxComparator.GREATER_THAN, requireSoftFluxCap = true, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        targetShieldHardFluxRegex.matches(canonicalTag) -> tooltipWithActivationCondition(
            baseTagTooltip("TargetShield") ?: "No description available.",
            fluxConditionWording(
                parseCanonicalFluxCondition(canonicalTag, targetShieldHardFluxRegex, FluxMetric.HARD, FluxComparator.GREATER_THAN, totalFluxCapExtractor = ::extractOptionalShieldTagTotalFluxCapFraction)!!
            )
        )

        pdSoftFluxRegex.matches(canonicalTag) -> "${pdTargetingRestrictionTooltip().removeSuffix(".")} while soft flux is greater than ${
            fluxConditionThresholdPercent(
                parseCanonicalFluxCondition(canonicalTag, pdSoftFluxRegex, FluxMetric.SOFT, FluxComparator.GREATER_THAN, requireSoftFluxCap = true)!!
            )
        } and total flux is below ${(Settings.softFluxTotalFluxCap() * 100f).toInt()}%."

        pdTotalFluxRegex.matches(canonicalTag) -> "${pdTargetingRestrictionTooltip().removeSuffix(".")} while total flux is greater than ${
            fluxConditionThresholdPercent(
                parseCanonicalFluxCondition(canonicalTag, pdTotalFluxRegex, FluxMetric.TOTAL, FluxComparator.GREATER_THAN)!!
            )
        }."

        pdHardFluxRegex.matches(canonicalTag) -> "${pdTargetingRestrictionTooltip().removeSuffix(".")} while hard flux is greater than ${
            fluxConditionThresholdPercent(
                parseCanonicalFluxCondition(canonicalTag, pdHardFluxRegex, FluxMetric.HARD, FluxComparator.GREATER_THAN)!!
            )
        }."

        opportunistAmmoRegex.matches(canonicalTag) -> "While ammo is less than ${
            extractPercentThresholdDisplayString(opportunistAmmoRegex, canonicalTag)
        }, only fires at opportune targets. Weapons without ammo are unaffected. The opportune-target thresholds below are percentages; 50 means 0.50."

        opportunistTunedRegex.matches(canonicalTag) -> {
            val match = opportunistTunedRegex.matchEntire(canonicalTag)
            "Makes the weapon much more hesitant to fire and forbids targeting missiles and fighters. " +
                "Kinetic weapons prefer shield factor above ${match?.groupValues?.getOrNull(1) ?: "?"}%, " +
                "HE/fragmentation weapons prefer shield factor below ${match?.groupValues?.getOrNull(2) ?: "?"}%, " +
                "and trigger-happiness is ${match?.groupValues?.getOrNull(3) ?: "?"}%."
        }

        pdAmmoRegex.matches(canonicalTag) -> "Restricts targeting to fighters and missiles while ammo is below ${
            extractPercentThresholdDisplayString(pdAmmoRegex, canonicalTag)
        }. Weapons without ammo and missile weapons are unaffected."

        noPdWasteRegex.matches(canonicalTag) -> noPdWasteTooltip(canonicalTag)

        noPdHealthRegex.matches(canonicalTag) -> noPdHealthTooltip(canonicalTag)

        targetPhaseDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("TargetPhase") ?: "No description available."

        avoidPhasedDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("AvoidPhased") ?: "No description available."

        prioWoundedDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("PrioWounded") ?: "No description available."

        avoidArmorRegex.matches(canonicalTag) -> "Fires when the shot is likely to hit shields (as TargetShield) OR a hull section " +
            "\nwhere the armor is low enough to achieve at least ${
                extractPercentThresholdDisplayString(
                    avoidArmorRegex,
                    canonicalTag
                )
            } " +
            "effectiveness vs armor." +
            "\nCombine with AvoidShield to also avoid shields (e.g. for frag weapons)."

        panicFireRegex.matches(canonicalTag) -> "Blindly fires without checking what the shot will hit while the ship" +
            " hull level is below ${extractPercentThresholdDisplayString(panicFireRegex, canonicalTag)}." +
            "\nFor AI-controlled ships, this puts the weapon group into ForceAutoFire mode once the hull threshold is reached."

        rangeRegex.matches(canonicalTag) -> "Only targets and fires at targets closer than ${
            extractPercentThresholdDisplayString(
                rangeRegex, canonicalTag
            )
        } of weapon range." +
            "\nUseful for weapons (especially missiles) with slow projectiles, such as sabots" +
            " or shotgun-style weapons, such as the devastator cannon." +
            "\nNote: This does not modify the actual range of the weapon, it only affects autofire behavior!"

        rofRegex.matches(canonicalTag) -> lowRofTooltip(canonicalTag)

        prioShieldsDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("PrioShields") ?: "No description available."

        prioHullDamageExclusionRegex.matches(canonicalTag) ->
            baseTagTooltip("PrioHull") ?: "No description available."

        else -> "No description available."
    }
}
