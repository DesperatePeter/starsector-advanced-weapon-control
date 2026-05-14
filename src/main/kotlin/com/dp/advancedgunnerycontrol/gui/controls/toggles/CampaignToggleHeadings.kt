package com.dp.advancedgunnerycontrol.gui.controls.toggles

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind

data class CampaignToggleHeading(
    val title: String,
    val expanded: Boolean,
    val active: Boolean,
    val subject: String,
    val inactiveKind: CampaignActionButtonKind = CampaignActionButtonKind.FILTER_CATEGORY,
    val activeKind: CampaignActionButtonKind = CampaignActionButtonKind.FILTER_CATEGORY_ACTIVE,
) {
    val label: String
        get() = "$title (${if (expanded) "-" else "+"})"

    val kind: CampaignActionButtonKind
        get() = if (active) activeKind else inactiveKind

    val tooltip: String
        get() {
            val action = if (expanded) "Collapse" else "Expand"
            val activeNote = if (active) " Active entries in this group remain applied while collapsed." else ""
            return "$action $title $subject.$activeNote"
        }
}
