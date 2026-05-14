package com.dp.advancedgunnerycontrol.gui.customlists.modal

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.scroll.CampaignTagScrollButtons
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalItemsHeight
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computeVerticalScrollSlice
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffsetByDelta
import com.dp.advancedgunnerycontrol.gui.controls.scroll.visibleScrollPageDelta

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max
import kotlin.math.min

/**
 * Black bordered list component used inside custom-list modals.
 * Renders Manage Tags and Ship Modes rows plus Custom List Changes review rows
 * with attached scroll indicators.
 */
internal object CustomListModalListRenderer {
    const val DEFAULT_VISIBLE_ROWS = 20
    const val LIST_INSET = 4f

    fun listPanelHeight(rowCount: Int, maxVisibleRows: Int = DEFAULT_VISIBLE_ROWS): Float {
        val visibleRows = rowCount.coerceIn(1, maxVisibleRows)
        return computeVerticalItemsHeight(
            visibleRows,
            itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            verticalGap = CampaignGuiStyle.MODAL_ROW_GAP
        ) + 2f * LIST_INSET
    }

    fun <T> render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        rowTop: Float,
        buttonY: Float,
        rows: List<T>,
        target: CustomListModalScrollTarget,
        scrollUpData: String,
        scrollDownData: String,
        currentOffset: Int,
        buttons: MutableList<ButtonBase<*>>,
        onScrollOffsetChanged: (Int) -> Unit,
        onDirty: () -> Unit,
        renderRow: (CustomPanelAPI, T, Float, Float) -> Unit,
    ): CustomListModalScrollRegion {
        val horizontalInset = CampaignGuiStyle.MODAL_COMPONENT_HORIZONTAL_INSET
        val availablePanelHeight = max(
            listPanelHeight(rowCount = 1),
            buttonY - CampaignGuiStyle.MODAL_BODY_ACTION_GAP - rowTop
        )
        val availableInnerHeight = max(CampaignGuiStyle.MODAL_ROW_HEIGHT, availablePanelHeight - 2f * LIST_INSET)
        val scrollSlice = computeVerticalScrollSlice(
            itemsToRender = rows,
            containerHeight = visibleRowAreaHeight(availableInnerHeight),
            currentOffset = currentOffset,
            itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            verticalGap = CampaignGuiStyle.MODAL_ROW_GAP,
        )
        val renderedPanelHeight = renderedListPanelHeight(
            rowCount = scrollSlice.items.size,
            hasAbove = scrollSlice.hasAbove,
            hasBelow = scrollSlice.hasBelow,
        ).coerceAtMost(availablePanelHeight)
        val panelTop = rowTop
        val listPanel = dialog.createCustomPanel(
            dialogWidth - 2f * (CampaignGuiStyle.MODAL_PADDING + horizontalInset),
            renderedPanelHeight,
            CampaignPanelPlugin(
                CampaignPanelType.CONTROL_PANEL,
                fillColor = CampaignGuiStyle.MODAL_SCROLL_LIST_FILL_COLOR,
                borderColor = CampaignGuiStyle.MODAL_SCROLL_LIST_BORDER_COLOR,
            )
        )
        dialog.addComponent(listPanel)
        listPanel.position.inTL(CampaignGuiStyle.MODAL_PADDING + horizontalInset, panelTop)

        val listInnerWidth = listPanel.position.width - 2f * LIST_INSET
        val rowOffset = scrollSlice.offset
        val maxOffset = scrollSlice.maxOffset
        if (rowOffset != currentOffset) {
            onScrollOffsetChanged(rowOffset)
        }

        val visibleRows = scrollSlice.items
        val pageDelta = visibleScrollPageDelta(scrollSlice)
        var currentRowTop = LIST_INSET
        if (scrollSlice.hasAbove) {
            val (_, button) = CampaignTagScrollButtons.addTagScrollIndicatorMomentaryButton(
                parent = listPanel,
                top = currentRowTop,
                symbol = CampaignGuiStyle.SCROLL_INDICATOR_ABOVE,
                data = scrollUpData,
                height = CampaignGuiStyle.MODAL_ROW_HEIGHT,
                x = LIST_INSET,
                width = listInnerWidth,
            ) {
                onScrollOffsetChanged(usefulVerticalScrollOffsetByDelta(rowOffset, -pageDelta, maxOffset))
                onDirty()
            }
            buttons.add(button)
            currentRowTop += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        }

        visibleRows.forEachIndexed { index, row ->
            val y = currentRowTop + index * (CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP)
            renderRow(listPanel, row, y, listInnerWidth)
        }

        if (scrollSlice.hasBelow) {
            val top = currentRowTop + computeVerticalItemsHeight(
                visibleRows.size,
                itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
                verticalGap = CampaignGuiStyle.MODAL_ROW_GAP
            ) + CampaignGuiStyle.MODAL_ROW_GAP
            val (_, button) = CampaignTagScrollButtons.addTagScrollIndicatorMomentaryButton(
                parent = listPanel,
                top = top,
                symbol = CampaignGuiStyle.SCROLL_INDICATOR_BELOW,
                data = scrollDownData,
                height = CampaignGuiStyle.MODAL_ROW_HEIGHT,
                x = LIST_INSET,
                width = listInnerWidth,
            ) {
                onScrollOffsetChanged(usefulVerticalScrollOffsetByDelta(rowOffset, pageDelta, maxOffset))
                onDirty()
            }
            buttons.add(button)
        }

        return CustomListModalScrollRegion(
            target = target,
            left = CampaignGuiStyle.MODAL_PADDING,
            right = dialogWidth - CampaignGuiStyle.MODAL_PADDING,
            bottom = dialogHeight - panelTop - renderedPanelHeight,
            top = dialogHeight - panelTop,
            maxOffset = maxOffset,
        )
    }

    private fun renderedListPanelHeight(rowCount: Int, hasAbove: Boolean, hasBelow: Boolean): Float {
        val controlCount = rowCount + hasAbove.toInt() + hasBelow.toInt()
        return computeVerticalItemsHeight(
            controlCount.coerceAtLeast(1),
            itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            verticalGap = CampaignGuiStyle.MODAL_ROW_GAP
        ) + 2f * LIST_INSET
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private fun visibleRowAreaHeight(rowAreaHeight: Float): Float {
        val maxVisibleHeight = computeVerticalItemsHeight(
            DEFAULT_VISIBLE_ROWS,
            itemHeight = CampaignGuiStyle.MODAL_ROW_HEIGHT,
            verticalGap = CampaignGuiStyle.MODAL_ROW_GAP
        )
        return min(rowAreaHeight, maxVisibleHeight).coerceAtLeast(CampaignGuiStyle.MODAL_ROW_HEIGHT)
    }
}
