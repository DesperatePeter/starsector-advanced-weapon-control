package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalItemsHeight

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

internal data class PresetActionModalLayout(
    val dialogHeight: Float,
    val contentWidth: Float,
    val bodyHeight: Float,
    val bodyParagraphs: List<CampaignModalBodyParagraph>,
    val bodyScrollable: Boolean,
    val reviewBody: PresetActionReviewBody? = null,
    val reviewMaxPreviewRows: Int = PresetActionReviewListMetrics.MAX_PREVIEW_ROWS,
)

internal const val PRESET_NO_TAGS_SELECTED_LABEL = "(No tags selected)"
internal const val PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL = "unknown weapon combination"

internal data class PresetReviewListRow(
    val label: String,
    val sprite: String? = null,
    val reserveIconSpace: Boolean = false,
)

internal data class PresetActionReviewBody(
    val intro: CampaignHighlightedText,
    val weaponRows: List<PresetReviewListRow>,
    val tagIntro: CampaignHighlightedText,
    val tagRows: List<PresetReviewListRow>,
    val closing: CampaignHighlightedText,
    val extraParagraphs: List<CampaignHighlightedText>,
    val footer: CampaignHighlightedText,
)

internal object PresetActionReviewListMetrics {
    const val MAX_PREVIEW_ROWS = 7
    const val ROW_HEIGHT = 18f
    const val ROW_GAP = 0f
    const val PANEL_HORIZONTAL_INSET = CampaignGuiStyle.MODAL_COMPONENT_HORIZONTAL_INSET
    const val LIST_INSET = 2f
    const val LIST_TOP_GAP = CampaignGuiStyle.MODAL_TEXT_COMPONENT_GAP
    const val LIST_BOTTOM_GAP = CampaignGuiStyle.MODAL_TEXT_COMPONENT_GAP
    const val PARAGRAPH_GAP = CampaignGuiStyle.MODAL_TEXT_COMPONENT_GAP
    const val WEAPON_ICON_SIZE = 16f

    fun panelHeight(
        rowCount: Int,
        maxPreviewRows: Int = MAX_PREVIEW_ROWS,
    ): Float {
        return computeVerticalItemsHeight(
            previewRowCount(rowCount, maxPreviewRows),
            itemHeight = ROW_HEIGHT,
            verticalGap = ROW_GAP,
        ) + 2f * LIST_INSET
    }

    fun previewRowCount(
        rowCount: Int,
        maxPreviewRows: Int = MAX_PREVIEW_ROWS,
    ): Int {
        val cappedMaxRows = maxPreviewRows.coerceIn(1, MAX_PREVIEW_ROWS)
        return if (rowCount > cappedMaxRows) {
            cappedMaxRows + 1
        } else {
            rowCount.coerceAtLeast(1)
        }
    }
}

internal data class PresetCleanBaselineKey(
    val shipId: String,
    val loadoutIndex: Int,
    val groupIndex: Int,
    val weaponKey: String,
)
