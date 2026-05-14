package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weaponais.mapBooleanToSpecificString
import org.lwjgl.input.Keyboard

fun priorityBoilerplateText(): String = "\nIncreases priority by a factor of ${Settings.prioXModifier()} (adjustable in Settings.editme)." +
    "\nCombine multiple Prio-tags to de-prioritize everything else."

internal fun baseTagTooltip(canonicalTag: String): String? = when (canonicalTag) {
    "AvoidShield" -> "Prioritizes targets with no shields, flanked shields, high flux, or shields turned off.\nFighter shields will ${
        mapBooleanToSpecificString(Settings.ignoreFighterShield(), "", "not")
    } be ignored (configurable in settings)." +
        "\nNo targeting restrictions."

    "TargetShield" -> "Prioritizes targeting shields. Stops firing against enemies with very high flux or no useful shield target." +
        "\nCan target, but not fire at, unshielded targets. Combine with Force tags if this weapon should still shoot unshielded targets.\nFighter shields will ${
            mapBooleanToSpecificString(Settings.ignoreFighterShield(), "", "not")
        } be ignored (configurable in settings)." +
        "\nTip: Keep one kinetic weapon on default to keep up pressure."

    "TargetShield+" -> "As TargetShield, but more aggressive." +
        "\nOnly stops shooting when flanking shields or shields are disabled." +
        "\nFighter shields will ${mapBooleanToSpecificString(Settings.ignoreFighterShield(), "", "not")} be ignored (configurable in settings)."

    "AvoidShield+" -> "As AvoidShield, but less aggressive." +
        "\nOnly shoots when flanking shields or shields are disabled." +
        "\nFighter shields will ${mapBooleanToSpecificString(Settings.ignoreFighterShield(), "", "not")} be ignored (configurable in settings)."

    "ConserveAmmo" -> "Weapon is much more hesitant to fire when ammo is below ${(Settings.conserveAmmo() * 100f).toInt()}%." +
        "\nNo targeting restrictions."

    "ConservePDAmmo" -> "When ammo is below ${(Settings.conservePDAmmo() * 100f).toInt()}%, only fires at fighters and missiles." +
        "\nFor non-PD weapons, only fighters are valid in that case." +
        "\nNo targeting restrictions."

    "Merge" -> "Press [${Keyboard.getKeyName(Settings.mergeHotkey())}] to merge all weapons with this tag into current weapon group. " +
        "\nFor player controlled ship only! Press [${Keyboard.getKeyName(Settings.mergeHotkey())}] again to undo." +
        "\nUse this tag to unleash big manually aimed barrages at your enemies!"

    "DisableTags" -> "Press [${Keyboard.getKeyName(Settings.disableTagsHotkey())}] to toggle all other AGC tags off for the currently selected weapon group." +
        "\nPress [${Keyboard.getKeyName(Settings.disableTagsHotkey())}] again to restore the group's saved tags." +
        "\nFor player controlled ships only. The tag list is preserved; only in-combat tag behavior is suppressed."

    "PrioFighter" -> "Prioritizes fighters over other targets when fighters are present.${priorityBoilerplateText()}"
    "PrioMissile" -> "Prioritizes missiles over other targets when missiles are present.${priorityBoilerplateText()}"
    "PrioShip" -> "Prioritizes non-fighter ships over other targets when ships are present.${priorityBoilerplateText()}"
    "PrioShields" -> "Prioritizes targets whose shields are likely to catch this weapon's shot.${priorityBoilerplateText()}"
    "PrioHull" -> "Prioritizes targets whose shields are absent, disabled, flanked, or stressed.${priorityBoilerplateText()}"
    "PrioClose" -> "Prioritizes nearby targets without restricting fire.${priorityBoilerplateText()}"
    "PrioFar" -> "Prioritizes distant in-range targets without restricting fire.${priorityBoilerplateText()}"
    else -> tagTooltips[canonicalTag]
}

internal val tagTooltips = mapOf(
    "PD" to pdTargetingRestrictionTooltip(),
    "PrioSmall" to "Prioritizes missiles, fighters, and smaller ships over larger ships.",
    "PrioBig" to "Prioritizes larger ships over smaller ships.\nNo targeting restrictions.",
    "NoPD" to "Forbids targeting missiles and prioritizes ships over fighters.",
    "TargetFighter" to "Restricts targeting to fighters.",
    "AvoidShield" to "Prioritizes targets with no shields, flanked shields, high flux, or shields turned off.\nFighter shields will ${
        mapBooleanToSpecificString(
            Settings.ignoreFighterShield(),
            "",
            "not"
        )
    } be ignored (configurable in settings)." +
        "\nNo targeting restrictions.",
    "TargetShield" to "Prioritizes targeting shields. Stops firing against enemies with very high flux or no useful shield target." +
        "\nCan target, but not fire at, unshielded targets. Combine with Force tags if this weapon should still shoot unshielded targets.\nFighter shields will ${
            mapBooleanToSpecificString(
                Settings.ignoreFighterShield(),
                "",
                "not"
            )
        } be ignored (configurable in settings)." +
        "\nTip: Keep one kinetic weapon on default to keep up pressure.",
    "TargetShield+" to "As TargetShield, but more aggressive." +
        "\nOnly stops shooting when flanking shields or shields are disabled." +
        "\nFighter shields will ${
            mapBooleanToSpecificString(
                Settings.ignoreFighterShield(),
                "",
                "not"
            )
        } be ignored (configurable in settings).",
    "AvoidShield+" to "As AvoidShield, but less aggressive." +
        "\nOnly shoots when flanking shields or shields are disabled." +
        "\nFighter shields will ${
            mapBooleanToSpecificString(
                Settings.ignoreFighterShield(),
                "",
                "not"
            )
        } be ignored (configurable in settings).",
    "NoFighter" to "Forbids targeting fighters.",
    "ConserveAmmo" to "Weapon is much more hesitant to fire when ammo is below ${(Settings.conserveAmmo() * 100f).toInt()}%." +
        "\nNo targeting restrictions.",
    "ConservePDAmmo" to "When ammo is below ${(Settings.conservePDAmmo() * 100f).toInt()}%, only fires at fighters and missiles." +
        "\nFor non-PD weapons, only fighters are valid in that case." +
        "\nNo targeting restrictions.",
    "Opportunist" to "Makes the weapon much more hesitant to fire and forbids targeting missiles and fighters. Useful for limited-ammo weapons.",
    "AvoidDebris" to "Does not fire when the shot is blocked by debris or asteroids." +
        "\nNote: This only affects the custom AI and the Opportunist mode already includes this option.",
    "TargetBig" to "Restricts targeting to larger ships.\nNo priority weighting.",
    "TargetSmall" to "Restricts targeting to smaller ships.\nNo priority weighting.",
    "ForceAutoFire" to "Forces AI-controlled ships to keep this weapon group on autofire, similar to the ForceAutoFire ship mode for all groups." +
        "\nNote: This modifies the ShipAI because the API cannot directly set a weapon group to autofire." +
        "\n      The ShipAI may still try to select this weapon group, but will be forced to deselect it again.",
    "DoNotShoot" to "Prevents this weapon group from firing unless another tag explicitly forces fire.\nUseful for weapons that should only fire under specific Force tag conditions.",
    "AvoidPhased" to "Ignores phase ships unless they cannot avoid the shot by phasing due to flux or cooldown." +
        "\nBest used on high-impact weapons; set some rapid-fire or beam weapons to TargetPhase to keep pressure on phase ships." +
        "\nNo targeting restrictions.",
    "TargetPhase" to "Restricts targeting to phase ships and prioritizes them. Does not care whether the target is currently phased." +
        "\nUseful for rapid-fire or beam weapons to keep up pressure on enemy phase coils." +
        "\nNo additional targeting restrictions beyond phase-ship targeting.",
    "ShipTarget" to "Restricts targeting to the selected ship target (R key). For AI-controlled ships, restricts targeting to the ShipAI maneuver target.",
    "NoMissile" to "Forbids targeting missiles.",
    "TargetOverloaded" to "Only targets and fires at overloaded or venting ships.",
    "NoShield" to "Simplified AvoidShield. Only fires at targets with no shields or shields turned off.",
    "Merge" to "Press [${Keyboard.getKeyName(Settings.mergeHotkey())}] to merge all weapons with this tag into current weapon group. " +
        "\nFor player controlled ship only! Press [${Keyboard.getKeyName(Settings.mergeHotkey())}] again to undo." +
        "\nUse this tag to unleash big manually aimed barrages at your enemies!",
    "DisableTags" to "Press [${Keyboard.getKeyName(Settings.disableTagsHotkey())}] to toggle all other AGC tags off for the currently selected weapon group." +
        "\nPress [${Keyboard.getKeyName(Settings.disableTagsHotkey())}] again to restore the group's saved tags." +
        "\nFor player controlled ships only. The tag list is preserved; only in-combat tag behavior is suppressed.",
    "SyncWindow" to "Synchronizes tagged weapons in the same weapon group into firing windows. " +
        "All tagged weapons wait until every tagged weapon is ready and on target, then fire together. " +
        "Fast weapons may keep firing until the longest-burst tagged weapon finishes, then the group waits to sync again.",
    "SyncVolley" to "Synchronizes tagged weapons in the same weapon group for one firing decision. " +
        "All tagged weapons wait until every tagged weapon is ready and on target, begin firing together, then wait to sync again. " +
        "Intrinsic weapon bursts and beams are allowed to finish naturally.",
    "Ambush" to "Tagged weapons in the same weapon group wait until every tagged weapon is ready and on the same target, then open fire together. " +
        "After the ambush starts, weapons keep prioritizing that target, but weapons that can no longer bear on it may fire at other valid targets. " +
        "The ambush resets when the target is lost by the whole group.",
    "PrioFighter" to "Prioritizes fighters over other targets when fighters are present.${priorityBoilerplateText()}",
    "PrioMissile" to "Prioritizes missiles over other targets when missiles are present.${priorityBoilerplateText()}",
    "PrioShip" to "Prioritizes non-fighter ships over other targets when ships are present.${priorityBoilerplateText()}",
    "PrioWounded" to "Prioritizes targets that have already taken hull damage.",
    "PrioWoundedPD" to "Prioritizes lower-effective-health PD targets. Fighter effective health uses the same weapon-relative durability estimate as AvoidPD(Waste>N%); missiles use remaining hitpoints.",
    "PrioHealthy" to "Prioritizes targets with high hull.",
    "PrioFocused" to "Prioritizes enemy ships that other allied ships are already targeting.",
    "PrioShields" to "Prioritizes targets whose shields are likely to catch this weapon's shot.${priorityBoilerplateText()}",
    "PrioHull" to "Prioritizes targets whose shields are absent, disabled, flanked, or stressed.${priorityBoilerplateText()}",
    "PrioClose" to "Prioritizes nearby targets without restricting fire.${priorityBoilerplateText()}",
    "PrioFar" to "Prioritizes distant in-range targets without restricting fire.${priorityBoilerplateText()}",
    "BlockBeams" to "Shoots enemies that are hitting this ship with beams, even when out of range. Intended mainly for the SVC Ink Spitter gun.",
    "CustomAI" to "Disables vanilla weapon AI for this group without adding other behavior. Useful for weapons that need fully custom AI handling.",
    "PrioDense" to "Prioritizes target-rich areas: big targets and targets with many other targets nearby. Useful for AoE weapons."
)
