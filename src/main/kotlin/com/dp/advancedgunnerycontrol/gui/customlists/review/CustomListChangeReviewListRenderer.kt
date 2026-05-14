package com.dp.advancedgunnerycontrol.gui.customlists.review

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.fs.starfarer.api.ui.CustomPanelAPI

internal object CustomListChangeReviewListRenderer {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        rows: List<CustomTagChangeReviewRow>,
        state: CustomListChangeReviewState,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: CustomListManagerModalCallbacks,
    ): CustomListModalScrollRegion? {
        val rowTop = CampaignGuiStyle.MODAL_PADDING +
            CampaignGuiStyle.MODAL_HEADING_HEIGHT +
            CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        val buttonY = dialogHeight - CampaignGuiStyle.MODAL_PADDING - CampaignGuiStyle.MODAL_ROW_HEIGHT
        return CustomListModalListRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            rowTop = rowTop,
            buttonY = buttonY,
            rows = rows,
            target = CustomListModalScrollTarget.REVIEW_CHANGES,
            scrollUpData = "custom_tag_change_review_scroll_up",
            scrollDownData = "custom_tag_change_review_scroll_down",
            currentOffset = callbacks.scrollOffset(CustomListModalScrollTarget.REVIEW_CHANGES),
            buttons = buttons,
            onScrollOffsetChanged = { callbacks.setScrollOffset(CustomListModalScrollTarget.REVIEW_CHANGES, it) },
            onDirty = callbacks.refresh,
        ) { listPanel, row, y, width ->
            renderChangeReviewRow(listPanel, row, y, width, state, buttons)
        }
    }

    fun buildRows(
        additions: List<String>,
        edits: Map<String, String>,
        removals: List<String>,
        shipModeAdditions: List<String> = emptyList(),
        shipModeEdits: Map<String, String> = emptyMap(),
        shipModeRemovals: List<String> = emptyList(),
        state: CustomListChangeReviewState,
    ): List<CustomTagChangeReviewRow> {
        return CustomTagChangeReviewRows.build(
            additions = additions,
            edits = edits,
            removals = removals,
            shipModeAdditions = shipModeAdditions,
            shipModeEdits = shipModeEdits,
            shipModeRemovals = shipModeRemovals,
            expandedSections = state.changeReviewExpandedSections(changeReviewSectionIds()),
        )
    }

    private fun renderChangeReviewRow(
        dialog: CustomPanelAPI,
        row: CustomTagChangeReviewRow,
        y: Float,
        width: Float,
        state: CustomListChangeReviewState,
        buttons: MutableList<ButtonBase<*>>,
    ) {
        CustomTagManagerRowRenderer.renderChangeReviewRow(
            dialog = dialog,
            row = row,
            y = y,
            width = width,
            buttons = buttons,
            onToggleSection = { sectionId ->
                state.toggleChangeReviewSection(sectionId, changeReviewSectionIds())
            },
        )
    }

    private fun changeReviewSectionIds(): List<String> =
        listOf(CustomListDraftKeys.ListSections.TAGS, CustomListDraftKeys.ListSections.SHIP_MODES) +
            listOf(CustomListDraftKeys.ListSections.TAGS, CustomListDraftKeys.ListSections.SHIP_MODES)
                .flatMap { parentSectionId ->
                    CustomTagChangeReviewSection.entries.map { section -> section.idFor(parentSectionId) }
                }
}
