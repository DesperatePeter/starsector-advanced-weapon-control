package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalItemsHeight

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import kotlin.math.max
import kotlin.math.min

internal object PresetActionModalLayoutBuilder {
    private const val BODY_MAX_LINES = 80
    private const val PRESET_BODY_TEXT_MIN_HEIGHT = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT
    val BODY_TO_CONTROLS_GAP: Float
        get() = CampaignGuiStyle.MODAL_BODY_CONTROL_GAP

    fun build(
        screenHeight: Float,
        dialogWidth: Float,
        state: PresetControlState,
        request: CampaignConfirmationModalRequest,
        reviewBody: PresetActionReviewBody? = null,
    ): PresetActionModalLayout {
        val contentWidth = dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING
        val controlsHeight = presetControlsHeight(state)
        val paragraphs = bodyParagraphs(request, contentWidth)
        val maxDialogHeight = CampaignGuiStyle.maxModalHeight(screenHeight, CampaignGuiStyle.MODAL_ROW_HEIGHT)
        val maxBodyHeight = max(
            CampaignGuiStyle.MODAL_ROW_HEIGHT,
            maxDialogHeight - fixedHeight(controlsHeight)
        )
        val reviewMaxPreviewRows = reviewBody?.let {
            fittedReviewMaxPreviewRows(
                body = it,
                contentWidth = contentWidth,
                maxBodyHeight = maxBodyHeight,
            )
        } ?: PresetActionReviewListMetrics.MAX_PREVIEW_ROWS
        val useReviewBody = reviewBody != null &&
            reviewBodyHeight(reviewBody, contentWidth, reviewMaxPreviewRows) <= maxBodyHeight + 0.5f
        val fullBodyHeight = if (reviewBody != null && useReviewBody) {
            reviewBodyHeight(reviewBody, contentWidth, reviewMaxPreviewRows)
        } else {
            campaignModalBodyHeight(paragraphs)
        }.coerceAtLeast(CampaignGuiStyle.MODAL_ROW_HEIGHT)
        val desiredHeight = modalHeightForBody(
            bodyHeight = fullBodyHeight + BODY_TO_CONTROLS_GAP + controlsHeight
        )
        val dialogHeight = min(
            desiredHeight,
            maxDialogHeight
        )
        val bodyHeight = min(
            fullBodyHeight,
            max(CampaignGuiStyle.MODAL_ROW_HEIGHT, dialogHeight - fixedHeight(controlsHeight))
        )
        return PresetActionModalLayout(
            dialogHeight = dialogHeight,
            contentWidth = contentWidth,
            bodyHeight = bodyHeight,
            bodyParagraphs = paragraphs,
            bodyScrollable = fullBodyHeight > bodyHeight + 0.5f,
            reviewBody = reviewBody?.takeIf { useReviewBody },
            reviewMaxPreviewRows = reviewMaxPreviewRows,
        )
    }

    private fun presetControlsHeight(state: PresetControlState): Float {
        val rowCount = if (state.pendingAction == PendingPresetAction.SAVE) 3 else 2
        return computeVerticalItemsHeight(
            rowCount,
            itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            verticalGap = CampaignGuiStyle.MODAL_ROW_GAP,
        )
    }

    private fun bodyParagraphs(
        request: CampaignConfirmationModalRequest,
        contentWidth: Float,
    ): List<CampaignModalBodyParagraph> {
        return campaignModalBodyParagraphs(
            paragraphs = request.richBody.takeIf { it.isNotEmpty() }
                ?: listOf(CampaignHighlightedText(request.body)),
            contentWidth = contentWidth,
            minRowHeight = PRESET_BODY_TEXT_MIN_HEIGHT,
            paragraphGap = PresetActionReviewListMetrics.PARAGRAPH_GAP,
            maxLines = BODY_MAX_LINES,
        )
    }

    private fun reviewBodyHeight(
        body: PresetActionReviewBody,
        contentWidth: Float,
        maxPreviewRows: Int,
    ): Float {
        return campaignModalBodyHeight(reviewParagraphs(body, contentWidth)) +
            PresetActionReviewListMetrics.panelHeight(body.weaponRows.size, maxPreviewRows) +
            PresetActionReviewListMetrics.panelHeight(body.tagRows.size, maxPreviewRows) +
            2f * PresetActionReviewListMetrics.LIST_TOP_GAP
    }

    private fun fittedReviewMaxPreviewRows(
        body: PresetActionReviewBody,
        contentWidth: Float,
        maxBodyHeight: Float,
    ): Int {
        return (PresetActionReviewListMetrics.MAX_PREVIEW_ROWS downTo 1)
            .firstOrNull { rows ->
                reviewBodyHeight(body, contentWidth, rows) <= maxBodyHeight + 0.5f
            }
            ?: 1
    }

    private fun reviewParagraphs(
        body: PresetActionReviewBody,
        contentWidth: Float,
    ): List<CampaignModalBodyParagraph> {
        val paragraphs = mutableListOf<CampaignModalBodyParagraph>()
        paragraphs.add(reviewParagraph(body.intro, contentWidth, 0f))
        paragraphs.add(reviewParagraph(body.tagIntro, contentWidth, PresetActionReviewListMetrics.LIST_BOTTOM_GAP))
        paragraphs.add(reviewParagraph(body.closing, contentWidth, PresetActionReviewListMetrics.LIST_BOTTOM_GAP))
        body.extraParagraphs.forEach { paragraph ->
            paragraphs.add(reviewParagraph(paragraph, contentWidth, PresetActionReviewListMetrics.PARAGRAPH_GAP))
        }
        paragraphs.add(reviewParagraph(body.footer, contentWidth, PresetActionReviewListMetrics.PARAGRAPH_GAP))
        return paragraphs
    }

    private fun reviewParagraph(
        paragraph: CampaignHighlightedText,
        contentWidth: Float,
        pad: Float,
    ): CampaignModalBodyParagraph {
        return CampaignModalBodyParagraph(
            paragraph = paragraph,
            pad = pad,
            layout = computeTextFitLayout(
                text = paragraph.text,
                availableWidth = contentWidth,
                minRowHeight = PRESET_BODY_TEXT_MIN_HEIGHT,
                horizontalPadding = 0f,
                verticalPadding = 0f,
                approxCharWidthPx = CampaignGuiStyle.ACTION_LABEL_APPROX_CHAR_WIDTH,
                lineHeightPx = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT,
                maxLines = BODY_MAX_LINES,
            )
        )
    }

    private fun modalHeightForBody(
        bodyHeight: Float,
        headingHeight: Float = CampaignGuiStyle.MODAL_HEADING_HEIGHT,
    ): Float {
        return CampaignGuiStyle.modalHeightForBody(
            bodyHeight = bodyHeight,
            headingHeight = headingHeight,
        )
    }

    private fun fixedHeight(controlsHeight: Float): Float {
        return CampaignGuiStyle.modalFixedHeightExcludingBody() +
            BODY_TO_CONTROLS_GAP +
            controlsHeight
    }
}
