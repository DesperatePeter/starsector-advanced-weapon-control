package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleHeading

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

internal object CustomTagManagerHeadingRowRenderer {
    fun renderListSectionHeading(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.ListSectionHeading,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleListSection: (String) -> Unit,
    ) {
        val label = "${row.label} (${if (row.expanded) "-" else "+"})"
        val template = CampaignGuiStyle.actionButtonTemplate(CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION)
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_list_manager_section:${row.id}",
            y = y,
            rowWidth = width,
            template = template,
            labelText = label,
            tooltip = if (row.expanded) {
                "Collapse ${row.label}."
            } else {
                "Expand ${row.label}."
            },
        ) {
            onToggleListSection(row.id)
        })
    }

    fun renderCategoryHeading(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.CategoryHeading,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleCategory: (String) -> Unit,
    ) {
        val heading = CampaignToggleHeading(
            title = row.category.title,
            expanded = row.expanded,
            active = row.active,
            subject = "custom tag archetypes",
            inactiveKind = CampaignActionButtonKind.TAG_CATEGORY,
            activeKind = CampaignActionButtonKind.TAG_CATEGORY_ACTIVE,
        )
        val template = CampaignGuiStyle.actionButtonTemplate(heading.kind)
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_manager_category:${row.category.title}",
            y = y,
            rowWidth = width,
            template = template,
            labelText = heading.label,
            tooltip = heading.tooltip,
        ) {
            onToggleCategory(row.category.title)
        })
    }

    fun renderArchetypeHeading(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.ArchetypeHeading,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onToggleArchetype: (String) -> Unit,
    ) {
        val expandedLabel = "${row.archetype.label} (${if (row.expanded) "-" else "+"})"
        val template = CampaignGuiStyle.actionButtonTemplate(
            if (row.active) CampaignActionButtonKind.TAG_CATEGORY_ACTIVE else CampaignActionButtonKind.TAG_CATEGORY
        )
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_manager_archetype:${row.archetype.id}",
            y = y,
            rowWidth = width,
            indentLevel = 1,
            template = template,
            labelText = expandedLabel,
            tooltip = if (row.expanded) {
                "Collapse ${row.archetype.label} custom tag variants."
            } else {
                "Expand ${row.archetype.label} custom tag variants."
            },
        ) {
            onToggleArchetype(row.archetype.id)
        })
    }
}
