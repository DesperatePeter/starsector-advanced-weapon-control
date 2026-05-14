package com.dp.advancedgunnerycontrol.gui.controls.weapontags

import com.dp.advancedgunnerycontrol.weapontags.disabledTagsForGroup
import com.dp.advancedgunnerycontrol.weapontags.getTagTooltip
import com.dp.advancedgunnerycontrol.weapontags.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.weapontags.shouldTagBeDisabled
import com.dp.advancedgunnerycontrol.weapontags.tagUnavailableReasonForWeaponGroup
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object TagButtonAvailability {
    fun isTagUnavailable(
        ship: FleetMemberAPI,
        group: Int,
        tag: String,
        currentTags: List<String>,
    ): Boolean {
        val otherTags = currentTags.toMutableList().apply { remove(tag) }
        return isIncompatibleWithExistingTags(tag, otherTags) || shouldTagBeDisabled(group, ship, tag)
    }

    fun unavailableTagsForSelection(
        ship: FleetMemberAPI,
        group: Int,
        candidateTags: List<String>,
        currentTags: List<String>,
    ): Set<String> {
        val disabledTags = disabledTagsForGroup(group, ship, candidateTags)
        return candidateTags
            .filter { tag -> tag in disabledTags || isIncompatibleWithExistingTags(tag, currentTags) }
            .toSet()
    }

    fun tooltipForTag(
        ship: FleetMemberAPI,
        group: Int,
        tag: String,
        unavailable: Boolean,
        selectedTags: List<String>,
    ): String {
        val baseTooltip = getTagTooltip(tag)
        if (!unavailable) return baseTooltip
        val reason = tagUnavailableReasonForWeaponGroup(group, ship, tag, selectedTags) ?: return baseTooltip
        return "$baseTooltip\n\nUnavailable: $reason"
    }
}
