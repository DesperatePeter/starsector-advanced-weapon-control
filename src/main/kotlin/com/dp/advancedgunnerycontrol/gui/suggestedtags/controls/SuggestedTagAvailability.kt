package com.dp.advancedgunnerycontrol.gui.suggestedtags.controls

import com.dp.advancedgunnerycontrol.weapontags.getTagTooltip
import com.dp.advancedgunnerycontrol.weapontags.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.weapontags.tagIncompatibilityReason

internal object SuggestedTagAvailability {
    fun isTagUnavailable(tag: String, currentTags: List<String>): Boolean {
        val otherTags = currentTags.toMutableList().apply { remove(tag) }
        return isIncompatibleWithExistingTags(tag, otherTags)
    }

    fun unavailableTagsForSelection(candidateTags: List<String>, currentTags: List<String>): Set<String> {
        return candidateTags
            .filter { tag -> isTagUnavailable(tag, currentTags) }
            .toSet()
    }

    fun tooltipForTag(tag: String, unavailable: Boolean, selectedTags: List<String>): String {
        val baseTooltip = getTagTooltip(tag)
        if (!unavailable) return baseTooltip
        val reason = tagIncompatibilityReason(tag, selectedTags) ?: return baseTooltip
        return "$baseTooltip\n\nUnavailable: $reason"
    }
}
