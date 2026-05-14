package com.dp.advancedgunnerycontrol.gui.customlists.review

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomTagChangeReviewRowRenderer {
    fun render(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleSection: (String) -> Unit,
    ) {
        when (row) {
            is CustomTagChangeReviewRow.ListHeading -> renderListHeading(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onToggleSection = onToggleSection,
            )
            is CustomTagChangeReviewRow.Heading -> renderHeading(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
                onToggleSection = onToggleSection,
            )
            is CustomTagChangeReviewRow.Entry -> renderEntry(
                dialog = dialog,
                row = row,
                y = y,
                width = width,
                buttons = buttons,
            )
        }
    }

    private fun renderListHeading(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow.ListHeading,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleSection: (String) -> Unit,
    ) {
        val hasContents = row.count > 0
        val label = if (hasContents) {
            "${row.label} (${row.count}) (${if (row.expanded) "-" else "+"})"
        } else {
            "${row.label} (${row.count})"
        }
        val template = CampaignGuiStyle.actionButtonTemplate(
            CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
            enabled = hasContents
        )
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_list_change_review_heading:${row.id}",
            y = y,
            rowWidth = width,
            template = template,
            labelText = label,
            tooltip = if (row.expanded) {
                "Collapse ${row.label} changes."
            } else {
                "Expand ${row.label} changes."
            },
        ) {
            if (hasContents) onToggleSection(row.id)
        })
    }

    private fun renderHeading(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow.Heading,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleSection: (String) -> Unit,
    ) {
        val hasContents = row.count > 0
        val label = if (hasContents) {
            "${row.section.label} (${row.count}) (${if (row.expanded) "-" else "+"})"
        } else {
            "${row.section.label} (${row.count})"
        }
        val template = CampaignGuiStyle.actionButtonTemplate(
            CampaignActionButtonKind.FILTER_CATEGORY,
            enabled = hasContents
        )
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_change_review_heading:${row.id}",
            y = y,
            rowWidth = width,
            indentLevel = 1,
            template = template,
            labelText = label,
            tooltip = if (row.expanded) {
                "Collapse ${row.section.label.lowercase()} changes."
            } else {
                "Expand ${row.section.label.lowercase()} changes."
            },
        ) {
            if (hasContents) onToggleSection(row.id)
        })
    }

    private fun renderEntry(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow.Entry,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
    ) {
        val template = CampaignGuiStyle.actionButtonTemplate(row.kind)
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_change_review_entry:${row.kind}:${row.label}",
            y = y,
            rowWidth = width,
            indentLevel = 2,
            template = template,
            labelText = row.label,
            centerConfirmCancelText = false,
        ) {})
    }
}
