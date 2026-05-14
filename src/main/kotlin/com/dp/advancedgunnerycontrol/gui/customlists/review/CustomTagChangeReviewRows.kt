package com.dp.advancedgunnerycontrol.gui.customlists.review

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*


internal object CustomTagChangeReviewRows {
    fun build(
        additions: List<String>,
        edits: Map<String, String>,
        removals: List<String>,
        shipModeAdditions: List<String> = emptyList(),
        shipModeEdits: Map<String, String> = emptyMap(),
        shipModeRemovals: List<String> = emptyList(),
        expandedSections: Set<String>,
    ): List<CustomTagChangeReviewRow> {
        val rows = mutableListOf<CustomTagChangeReviewRow>()

        fun addSection(
            parentSectionId: String,
            section: CustomTagChangeReviewSection,
            labels: List<String>,
        ) {
            val sectionId = section.idFor(parentSectionId)
            val isExpanded = sectionId in expandedSections
            rows += CustomTagChangeReviewRow.Heading(sectionId, section, labels.size, isExpanded)
            if (isExpanded) {
                labels.forEach { label ->
                    rows += CustomTagChangeReviewRow.Entry(label, section.kind)
                }
            }
        }

        val tagChangeCount = additions.size + edits.size + removals.size
        val tagsExpanded = CustomListDraftKeys.ListSections.TAGS in expandedSections
        rows += CustomTagChangeReviewRow.ListHeading(
            id = CustomListDraftKeys.ListSections.TAGS,
            label = CustomTagManagerSectionLabels.TAGS,
            count = tagChangeCount,
            expanded = tagsExpanded,
        )
        if (tagsExpanded) {
            addSection(
                CustomListDraftKeys.ListSections.TAGS,
                CustomTagChangeReviewSection.ADDED,
                sortedDisplayTags(additions),
            )
            addSection(
                CustomListDraftKeys.ListSections.TAGS,
                CustomTagChangeReviewSection.MODIFIED,
                sortedDisplayEdits(edits),
            )
            addSection(
                CustomListDraftKeys.ListSections.TAGS,
                CustomTagChangeReviewSection.REMOVED,
                sortedDisplayTags(removals),
            )
        }

        val shipModeChangeCount = shipModeAdditions.size + shipModeEdits.size + shipModeRemovals.size
        val shipModesExpanded = CustomListDraftKeys.ListSections.SHIP_MODES in expandedSections
        rows += CustomTagChangeReviewRow.ListHeading(
            id = CustomListDraftKeys.ListSections.SHIP_MODES,
            label = CustomTagManagerSectionLabels.SHIP_MODES,
            count = shipModeChangeCount,
            expanded = shipModesExpanded,
        )
        if (shipModesExpanded) {
            addSection(
                CustomListDraftKeys.ListSections.SHIP_MODES,
                CustomTagChangeReviewSection.ADDED,
                sortedDisplayShipModes(shipModeAdditions),
            )
            addSection(
                CustomListDraftKeys.ListSections.SHIP_MODES,
                CustomTagChangeReviewSection.MODIFIED,
                sortedDisplayShipModeEdits(shipModeEdits),
            )
            addSection(
                CustomListDraftKeys.ListSections.SHIP_MODES,
                CustomTagChangeReviewSection.REMOVED,
                sortedDisplayShipModes(shipModeRemovals),
            )
        }
        return rows
    }

    private fun displayTag(tag: String): String = EditableWeaponTagDefinitions.displayName(tag)

    private fun sortedDisplayTags(tags: List<String>): List<String> {
        val labels = mutableListOf<String>()
        for (tag in tags) {
            labels += displayTag(tag)
        }
        labels.sort()
        return labels
    }

    private fun sortedDisplayEdits(edits: Map<String, String>): List<String> {
        val labels = mutableListOf<String>()
        for ((source, edited) in edits) {
            labels += "${displayTag(source)} -> ${displayTag(edited)}"
        }
        labels.sort()
        return labels
    }

    private fun sortedDisplayShipModes(modes: List<String>): List<String> {
        val labels = mutableListOf<String>()
        for (mode in modes) {
            labels += shipModeDisplayName(mode)
        }
        labels.sort()
        return labels
    }

    private fun sortedDisplayShipModeEdits(edits: Map<String, String>): List<String> {
        val labels = mutableListOf<String>()
        for ((source, edited) in edits) {
            labels += "${shipModeDisplayName(source)} -> ${shipModeDisplayName(edited)}"
        }
        labels.sort()
        return labels
    }
}
