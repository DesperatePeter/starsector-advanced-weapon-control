package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max

/**
 * Structured Save/Load review body component.
 * Used in per-weapon-group Save/Load modals to show capped weapon and tag
 * previews instead of long inline text.
 */
internal object PresetActionReviewRenderer {
    private const val TAG_ROW_MARKER = "-"
    private const val TAG_ROW_MARKER_LEFT_PADDING = 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING
    private const val TRUNCATION_MARKER = "..."

    fun render(
        body: CustomPanelAPI,
        review: PresetActionReviewBody,
        contentWidth: Float,
        maxPreviewRows: Int = PresetActionReviewListMetrics.MAX_PREVIEW_ROWS,
    ) {
        var y = 0f
        y = renderParagraph(body, review.intro, contentWidth, y, 0f)
        y = renderList(
            body = body,
            rows = review.weaponRows,
            contentWidth = contentWidth,
            top = y,
            emptyLabel = PRESET_UNKNOWN_WEAPON_COMBINATION_LABEL,
            reserveTruncationIconSpace = true,
            maxPreviewRows = maxPreviewRows,
        )
        y = renderParagraph(body, review.tagIntro, contentWidth, y, PresetActionReviewListMetrics.LIST_BOTTOM_GAP)
        y = renderList(
            body = body,
            rows = review.tagRows,
            contentWidth = contentWidth,
            top = y,
            iconPlaceholder = TAG_ROW_MARKER,
            emptyLabel = PRESET_NO_TAGS_SELECTED_LABEL,
            reserveTruncationIconSpace = true,
            maxPreviewRows = maxPreviewRows,
        )
        y = renderParagraph(body, review.closing, contentWidth, y, PresetActionReviewListMetrics.LIST_BOTTOM_GAP)
        review.extraParagraphs.forEach { paragraph ->
            y = renderParagraph(body, paragraph, contentWidth, y, PresetActionReviewListMetrics.PARAGRAPH_GAP)
        }
        renderParagraph(body, review.footer, contentWidth, y, PresetActionReviewListMetrics.PARAGRAPH_GAP)
    }

    private fun renderParagraph(
        body: CustomPanelAPI,
        paragraph: CampaignHighlightedText,
        contentWidth: Float,
        top: Float,
        pad: Float,
    ): Float {
        val bodyParagraph = CampaignModalBodyParagraph(
            paragraph = paragraph,
            pad = pad,
            layout = computeTextFitLayout(
                text = paragraph.text,
                availableWidth = contentWidth,
                minRowHeight = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT,
                horizontalPadding = 0f,
                verticalPadding = 0f,
                approxCharWidthPx = CampaignGuiStyle.ACTION_LABEL_APPROX_CHAR_WIDTH,
                lineHeightPx = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT,
                maxLines = 80,
            )
        )
        val element = body.createUIElement(contentWidth, bodyParagraph.layout.renderHeight, false)
        renderCampaignHighlightedTextBody(element, listOf(bodyParagraph.copy(pad = 0f)))
        val y = top + pad
        body.addUIElement(element).inTL(0f, y)
        return y + bodyParagraph.layout.renderHeight
    }

    private fun renderList(
        body: CustomPanelAPI,
        rows: List<PresetReviewListRow>,
        contentWidth: Float,
        top: Float,
        iconPlaceholder: String? = null,
        emptyLabel: String,
        reserveTruncationIconSpace: Boolean = false,
        maxPreviewRows: Int,
    ): Float {
        val visibleRows = previewRows(rows, reserveTruncationIconSpace, maxPreviewRows, emptyLabel)
        val panelWidth = contentWidth - 2f * PresetActionReviewListMetrics.PANEL_HORIZONTAL_INSET
        val panelHeight = PresetActionReviewListMetrics.panelHeight(visibleRows.size)
        val panelTop = top + PresetActionReviewListMetrics.LIST_TOP_GAP
        val listPanel = body.createCustomPanel(
            panelWidth,
            panelHeight,
            CampaignPanelPlugin(
                CampaignPanelType.WEAPON_PANEL,
                fillColor = CampaignGuiStyle.MODAL_REVIEW_LIST_FILL_COLOR,
            )
        )
        body.addComponent(listPanel)
        listPanel.position.inTL(PresetActionReviewListMetrics.PANEL_HORIZONTAL_INSET, panelTop)
        val innerWidth = panelWidth - 2f * PresetActionReviewListMetrics.LIST_INSET
        var y = PresetActionReviewListMetrics.LIST_INSET
        visibleRows.forEach { row ->
            renderRow(listPanel, row, y, innerWidth, iconPlaceholder)
            y += PresetActionReviewListMetrics.ROW_HEIGHT + PresetActionReviewListMetrics.ROW_GAP
        }
        return panelTop + panelHeight
    }

    private fun previewRows(
        rows: List<PresetReviewListRow>,
        reserveTruncationIconSpace: Boolean,
        maxPreviewRows: Int,
        emptyLabel: String,
    ): List<PresetReviewListRow> {
        if (rows.isEmpty()) return listOf(PresetReviewListRow(emptyLabel))
        val cappedMaxRows = maxPreviewRows.coerceIn(1, PresetActionReviewListMetrics.MAX_PREVIEW_ROWS)
        if (rows.size <= cappedMaxRows) return rows
        return rows.take(cappedMaxRows) +
            PresetReviewListRow(TRUNCATION_MARKER, reserveIconSpace = reserveTruncationIconSpace)
    }

    private fun renderRow(
        listPanel: CustomPanelAPI,
        row: PresetReviewListRow,
        y: Float,
        width: Float,
        iconPlaceholder: String?,
    ) {
        val rowPanel = listPanel.createCustomPanel(
            width,
            PresetActionReviewListMetrics.ROW_HEIGHT,
            CampaignPanelPlugin(CampaignPanelType.WEAPON_PANEL, fillColor = CampaignGuiStyle.TRANSPARENT_PANEL_FILL_COLOR)
        )
        listPanel.addComponent(rowPanel)
        rowPanel.position.inTL(PresetActionReviewListMetrics.LIST_INSET, y)
        val iconSize = if (
            row.sprite.isNullOrBlank() &&
            iconPlaceholder == null &&
            !row.reserveIconSpace
        ) {
            0f
        } else {
            PresetActionReviewListMetrics.WEAPON_ICON_SIZE
        }
        if (!row.sprite.isNullOrBlank()) {
            val imagePanel = rowPanel.createUIElement(iconSize, PresetActionReviewListMetrics.ROW_HEIGHT, false)
            imagePanel.addImage(row.sprite, iconSize, iconSize, 0f)
            rowPanel.addUIElement(imagePanel).inTL(0f, max(0f, (PresetActionReviewListMetrics.ROW_HEIGHT - iconSize) / 2f))
        } else if (iconPlaceholder != null && !row.reserveIconSpace) {
            CampaignControlLabels.renderTagLabel(
                panel = rowPanel,
                text = iconPlaceholder,
                width = iconSize - TAG_ROW_MARKER_LEFT_PADDING,
                height = PresetActionReviewListMetrics.ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
                x = TAG_ROW_MARKER_LEFT_PADDING,
                y = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            )
        }
        val textLeft = if (iconSize > 0f) iconSize + CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING else 0f
        CampaignControlLabels.renderTagLabel(
            panel = rowPanel,
            text = row.label,
            width = width - textLeft - CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
            height = PresetActionReviewListMetrics.ROW_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            x = textLeft,
            y = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
        )
    }
}
