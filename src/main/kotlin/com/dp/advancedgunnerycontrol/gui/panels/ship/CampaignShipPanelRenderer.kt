package com.dp.advancedgunnerycontrol.gui.panels.ship

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.agcShortShipId
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.max
import kotlin.math.min

/**
 * Left-column Ship panel component.
 * Shows the selected ship sprite plus ship name, hull, loadout, and short AGC id.
 */
object CampaignShipPanelRenderer {
    private const val SHIP_INFO_LINE_HEIGHT = 16f
    private const val SHIP_INFO_ROW_GAP = 8f
    private const val SHIP_IMAGE_TO_INFO_GAP = 10f
    private const val SHIP_INFO_MAX_LINES = 2
    private const val SHIP_IMAGE_PANEL_TOP_GAP = 2f
    private const val SHIP_IMAGE_SIDE_MARGIN = 10f
    private const val SHIP_IMAGE_MIN_SIZE = 76f

    private data class ShipInfoRowLayout(
        val label: String,
        val wrappedValue: String,
        val height: Float,
    )

    fun render(panel: CustomPanelAPI, ship: FleetMemberAPI, headingHeight: Float, renderHeading: Boolean = true) {
        if (renderHeading) {
            CampaignControlLabels.addCampaignPanelHeading(panel, "Ship", headingHeight = headingHeight)
        }

        val bodyTop = CampaignGuiStyle.PANEL_PADDING + headingHeight + SHIP_IMAGE_PANEL_TOP_GAP
        val innerWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
        val labelWidth = labelWidth(innerWidth)
        val valueWidth = valueWidth(innerWidth)
        val rows = shipInfoRows(ship, valueWidth)
        val rowsHeight = infoRowsHeight(rows)
        val availableImageHeight = panel.position.height -
            bodyTop -
            rowsHeight -
            CampaignGuiStyle.PANEL_PADDING
        val spriteSize = min(innerWidth - 2f * SHIP_IMAGE_SIDE_MARGIN, availableImageHeight).coerceAtLeast(SHIP_IMAGE_MIN_SIZE)

        val imagePanel = panel.createUIElement(spriteSize, spriteSize, false)
        imagePanel.addImage(ship.hullSpec.spriteName, spriteSize, spriteSize, 0f)
        panel.addUIElement(imagePanel).inTL((panel.position.width - spriteSize) / 2f, bodyTop)

        var rowTop = bodyTop + spriteSize + SHIP_IMAGE_TO_INFO_GAP
        rows.forEach { row ->
            renderInfoRow(panel, row, rowTop, labelWidth, valueWidth)
            rowTop += row.height + SHIP_INFO_ROW_GAP
        }
    }

    fun preferredHeight(ship: FleetMemberAPI, panelWidth: Float, headingHeight: Float): Float {
        val bodyTop = CampaignGuiStyle.PANEL_PADDING + headingHeight + SHIP_IMAGE_PANEL_TOP_GAP
        val innerWidth = panelWidth - 2f * CampaignGuiStyle.PANEL_PADDING
        val spriteSize = (innerWidth - 2f * SHIP_IMAGE_SIDE_MARGIN).coerceAtLeast(SHIP_IMAGE_MIN_SIZE)
        val rows = shipInfoRows(ship, valueWidth(innerWidth))
        return bodyTop + spriteSize + infoRowsHeight(rows) + CampaignGuiStyle.PANEL_PADDING
    }

    fun minimumHeight(ship: FleetMemberAPI, panelWidth: Float, headingHeight: Float): Float {
        val bodyTop = CampaignGuiStyle.PANEL_PADDING + headingHeight + SHIP_IMAGE_PANEL_TOP_GAP
        val innerWidth = panelWidth - 2f * CampaignGuiStyle.PANEL_PADDING
        val rows = shipInfoRows(ship, valueWidth(innerWidth))
        return bodyTop + SHIP_IMAGE_MIN_SIZE + infoRowsHeight(rows) + CampaignGuiStyle.PANEL_PADDING
    }

    private fun shipInfoRow(label: String, value: String, valueWidth: Float): ShipInfoRowLayout {
        val maxCharsPerLine = max(8, ((valueWidth - 8f) / 6.4f).toInt())
        val wrappedValue = fitAgcTextByChars(value, maxCharsPerLine, SHIP_INFO_MAX_LINES)
        val lineCount = wrappedValue.split("\n").size.coerceIn(1, SHIP_INFO_MAX_LINES)
        return ShipInfoRowLayout(
            label = label,
            wrappedValue = wrappedValue,
            height = lineCount * SHIP_INFO_LINE_HEIGHT
        )
    }

    private fun shipInfoRows(ship: FleetMemberAPI, valueWidth: Float): List<ShipInfoRowLayout> {
        return listOf(
            shipInfoRow("Hull", ship.hullSpec.hullName, valueWidth),
            shipInfoRow("Name", ship.shipName, valueWidth),
            shipInfoRow("Variant", ship.variant?.displayName?.ifBlank { "Default" } ?: "Default", valueWidth),
            shipInfoRow("ID", shortShipId(ship), valueWidth),
        )
    }

    private fun shortShipId(ship: FleetMemberAPI): String {
        return agcShortShipId(ship)
    }

    private fun infoRowsHeight(rows: List<ShipInfoRowLayout>): Float {
        return rows.sumOf { it.height.toDouble() }.toFloat() +
            SHIP_IMAGE_TO_INFO_GAP +
            SHIP_INFO_ROW_GAP * rows.size
    }

    private fun labelWidth(innerWidth: Float): Float = innerWidth * 0.34f
    private fun valueWidth(innerWidth: Float): Float = innerWidth - labelWidth(innerWidth)

    private fun renderInfoRow(
        panel: CustomPanelAPI,
        row: ShipInfoRowLayout,
        top: Float,
        labelWidth: Float,
        valueWidth: Float,
    ) {
        val labelPanel = panel.createUIElement(labelWidth, row.height, false)
        labelPanel.addAgcText("${row.label}:", 0f)
        panel.addUIElement(labelPanel).inTL(CampaignGuiStyle.PANEL_PADDING, top)

        val valuePanel = panel.createUIElement(valueWidth, row.height, false)
        valuePanel.addAgcText(row.wrappedValue, 0f)
        panel.addUIElement(valuePanel).inTL(CampaignGuiStyle.PANEL_PADDING + labelWidth, top)
    }
}
