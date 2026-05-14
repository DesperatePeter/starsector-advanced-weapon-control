package com.dp.advancedgunnerycontrol.gui.style

import com.dp.advancedgunnerycontrol.gui.foundation.WrapGridMetrics
import com.dp.advancedgunnerycontrol.gui.foundation.WrappedLabelLayout
import com.dp.advancedgunnerycontrol.gui.foundation.computeWrapGridMetrics
import com.dp.advancedgunnerycontrol.gui.foundation.computeWrappedLabelLayout
import kotlin.math.max
import kotlin.math.min

internal object CampaignGuiLayoutStyle {
    fun maxModalHeight(screenHeight: Float, minimum: Float = CampaignGuiStyle.MODAL_ROW_HEIGHT): Float {
        return min(
            screenHeight * CampaignGuiStyle.MODAL_MAX_HEIGHT_FRACTION,
            screenHeight - 2f * CampaignGuiStyle.PANEL_PADDING
        ).coerceAtLeast(minimum)
    }

    fun modalHeightForBody(
        bodyHeight: Float,
        headingHeight: Float = CampaignGuiStyle.MODAL_HEADING_HEIGHT,
        footerHeight: Float = CampaignGuiStyle.MODAL_ROW_HEIGHT,
        titleBodyGap: Float = CampaignGuiStyle.MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = CampaignGuiStyle.MODAL_BODY_ACTION_GAP,
    ): Float {
        return CampaignGuiStyle.MODAL_PADDING +
            headingHeight +
            titleBodyGap +
            bodyHeight +
            bodyActionGap +
            footerHeight +
            CampaignGuiStyle.MODAL_PADDING
    }

    fun modalFixedHeightExcludingBody(
        headingHeight: Float = CampaignGuiStyle.MODAL_HEADING_HEIGHT,
        footerHeight: Float = CampaignGuiStyle.MODAL_ROW_HEIGHT,
        titleBodyGap: Float = CampaignGuiStyle.MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = CampaignGuiStyle.MODAL_BODY_ACTION_GAP,
    ): Float {
        return modalHeightForBody(
            bodyHeight = 0f,
            headingHeight = headingHeight,
            footerHeight = footerHeight,
            titleBodyGap = titleBodyGap,
            bodyActionGap = bodyActionGap,
        )
    }

    fun modalBodyHeightForDialog(
        dialogHeight: Float,
        headingHeight: Float = CampaignGuiStyle.MODAL_HEADING_HEIGHT,
        footerHeight: Float = CampaignGuiStyle.MODAL_ROW_HEIGHT,
        minimumBodyHeight: Float = CampaignGuiStyle.MODAL_ROW_HEIGHT,
        titleBodyGap: Float = CampaignGuiStyle.MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = CampaignGuiStyle.MODAL_BODY_ACTION_GAP,
    ): Float {
        return max(
            minimumBodyHeight,
            dialogHeight - modalFixedHeightExcludingBody(
                headingHeight = headingHeight,
                footerHeight = footerHeight,
                titleBodyGap = titleBodyGap,
                bodyActionGap = bodyActionGap,
            )
        )
    }

    fun actionLabelLayout(labelText: String, width: Float, maxLines: Int): WrappedLabelLayout {
        return computeWrappedLabelLayout(
            text = labelText,
            rowWidth = width - 2f * CampaignGuiStyle.ACTION_ROW_PADDING,
            minButtonHeight = 18f,
            horizontalPadding = 2f * CampaignGuiStyle.ACTION_ROW_PADDING,
            verticalPadding = 2f * CampaignGuiStyle.ACTION_ROW_PADDING,
            approxCharWidthPx = CampaignGuiStyle.ACTION_LABEL_APPROX_CHAR_WIDTH,
            lineHeightPx = CampaignGuiStyle.ACTION_LABEL_LINE_HEIGHT,
            maxLines = maxLines
        )
    }

    fun campaignTagGridMetrics(tags: List<String>, panelWidth: Float, panelHeight: Float): WrapGridMetrics {
        val itemHeight = tags.map { tag ->
            computeWrappedLabelLayout(
                text = tag,
                rowWidth = panelWidth - 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
                minButtonHeight = CampaignGuiStyle.TAG_ITEM_HEIGHT,
                horizontalPadding = 2f * CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
                verticalPadding = 2f * CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
                maxLines = 1
            ).rowHeight
        }.maxOrNull() ?: CampaignGuiStyle.TAG_ITEM_HEIGHT
        return computeWrapGridMetrics(
            itemCount = max(tags.size, 1),
            availableWidth = panelWidth,
            availableHeight = panelHeight,
            minItemWidth = CampaignGuiStyle.TAG_ITEM_MIN_WIDTH,
            itemHeight = itemHeight,
            horizontalGap = CampaignGuiStyle.TAG_ITEM_HGAP,
            verticalGap = CampaignGuiStyle.TAG_ITEM_VGAP,
            maxColumns = 1
        )
    }
}
