package com.dp.advancedgunnerycontrol.gui.customlists.manager

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

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

import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomTagManagerTagRowRenderer {
    fun renderTagEntry(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.TagEntry,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        editSourceForTag: (String) -> String?,
        onToggleTag: (CustomTagManagerRow.TagEntry, String?) -> Unit,
        onEditTag: (tag: String, pendingAddition: Boolean, sourceTag: String) -> Unit,
    ) {
        val status = CustomTagManagerRows.status(row)
        val label = CustomTagManagerRows.tagLabel(row.tag, status)
        val editSource = editSourceForTag(row.tag)
        val template = CampaignGuiStyle.actionButtonTemplate(status.kind)
        val managerButton = CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_manager_tag:${row.tag}",
            y = y,
            rowWidth = width,
            indentLevel = 2,
            template = template,
            labelText = label,
            highlightTokens = CustomTagManagerRows.highlightTokens(label, status),
            tooltip = CustomTagManagerRows.tagTooltip(row, status, editSource),
            centerConfirmCancelText = false,
        ) {
            onToggleTag(row, editSource)
        }
        if (!row.markedForRemoval) {
            managerButton.onRightClick {
                onEditTag(
                    row.tag,
                    row.pendingAddition,
                    if (row.pendingEdit) editSource ?: row.tag else row.tag,
                )
            }
        }
        buttons.add(managerButton)
    }

    fun renderAddTagEntry(
        dialog: CustomPanelAPI,
        row: CustomTagManagerRow.AddTagEntry,
        y: Float,
        width: Float,
        buttons: MutableList<ButtonBase<*>>,
        onAddDefinition: (EditableWeaponTagDefinition) -> Unit,
        onAddDirectTag: (String) -> Unit,
    ) {
        val archetype = row.archetype
        val template = CampaignGuiStyle.actionButtonTemplate(CampaignActionButtonKind.ADD_TAG)
        buttons.add(CustomTagManagerRowButton.add(
            parent = dialog,
            data = "custom_tag_manager_add:${archetype.id}",
            y = y,
            rowWidth = width,
            indentLevel = 2,
            template = template,
            labelText = "Add Tag",
            tooltip = "Add a ${archetype.label} custom tag.",
        ) {
            when {
                archetype.definition != null -> onAddDefinition(archetype.definition)
                archetype.directTag != null -> onAddDirectTag(archetype.directTag)
            }
        })
    }
}
