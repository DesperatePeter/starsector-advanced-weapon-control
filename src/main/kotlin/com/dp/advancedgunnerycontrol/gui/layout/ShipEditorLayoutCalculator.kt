package com.dp.advancedgunnerycontrol.gui.layout

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.panels.ship.CampaignShipPanelRenderer

import com.fs.starfarer.api.fleet.FleetMemberAPI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

internal enum class CollapsiblePanelKey(val id: String, val title: String) {
    SHIP("ship", "Ship"),
    OPTIONS("options", "Options"),
}

internal object ShipEditorLayoutCalculator {
    private const val LEFT_COLUMN_WIDTH_FRACTION = 0.1667f
    private const val LEFT_COLUMN_WIDTH_MIN = 185f
    private const val LEFT_COLUMN_WIDTH_MAX = 240f
    private const val SHIP_MODE_HEIGHT_MIN = 96f
    private const val SHIP_MODE_MIN_VISIBLE_ROWS = 5
    private const val SHIP_MODE_PREFERRED_VISIBLE_ROWS = 9
    private const val SHIP_MODE_PANEL_BOTTOM_PADDING = 0f
    private const val OPTIONS_HEIGHT_FALLBACK_MIN = 120f

    fun compute(
        panelWidth: Float,
        panelHeight: Float,
        ship: FleetMemberAPI,
        shipModes: List<String>,
        optionsPreferredHeightProvider: ((Float) -> Float)?,
        stableOptionsPreferredHeightProvider: ((Float) -> Float)?,
        modifiersPreferredHeightProvider: (() -> Float)?,
        isCollapsed: (CollapsiblePanelKey) -> Boolean,
    ): ShipEditorLayout {
        val leftColumnWidth = sharedLeftColumnWidth(panelWidth)
        val expandedShipPanelHeight = CampaignShipPanelRenderer.minimumHeight(
            ship = ship,
            panelWidth = leftColumnWidth,
            headingHeight = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT
        )
        val shipPanelHeight = heightForCollapsiblePanel(
            collapsed = isCollapsed(CollapsiblePanelKey.SHIP),
            expandedHeight = expandedShipPanelHeight,
        )
        val preferredShipModeHeight = estimateShipModePanelHeight(shipModes)
        val minimumShipModeHeight = minimumVisibleShipModePanelHeight()
        val expandedModifiersHeight = modifiersPreferredHeightProvider?.invoke() ?: 0f
        val modifiersHeight = if (isCollapsed(CollapsiblePanelKey.OPTIONS)) 0f else expandedModifiersHeight
        val optionsContentWidth = leftColumnWidth - 2f * CampaignGuiStyle.PANEL_PADDING
        val expandedCurrentOptionsHeight = optionsPreferredHeightProvider?.invoke(optionsContentWidth)
            ?: max(
                OPTIONS_HEIGHT_FALLBACK_MIN,
                panelHeight - expandedShipPanelHeight - expandedModifiersHeight - preferredShipModeHeight
            )
        val rawCurrentOptionsHeight = heightForCollapsiblePanel(
            collapsed = isCollapsed(CollapsiblePanelKey.OPTIONS),
            expandedHeight = expandedCurrentOptionsHeight,
        )
        val expandedStableOptionsHeight =
            stableOptionsPreferredHeightProvider?.invoke(optionsContentWidth) ?: expandedCurrentOptionsHeight
        val minimumOptionsHeight = collapsedPanelHeight()
        val stableOptionsHeight = expandedStableOptionsHeight.coerceAtMost(
            max(
                minimumOptionsHeight,
                panelHeight -
                    expandedShipPanelHeight -
                    expandedModifiersHeight -
                    minimumShipModeHeight
            )
        )
        val shipModeReserveHeight = minimumShipModeHeight
        val currentOptionsHeight = rawCurrentOptionsHeight.coerceAtMost(
            max(
                minimumOptionsHeight,
                panelHeight - shipPanelHeight - modifiersHeight - shipModeReserveHeight
            )
        )
        val remainingForShipModes = panelHeight - shipPanelHeight - currentOptionsHeight - modifiersHeight
        val desiredShipModeHeight = tightShipModePanelHeightForAvailable(shipModes, remainingForShipModes)
            .coerceAtLeast(minimumShipModeHeight)
        val shipModeHeight = desiredShipModeHeight.coerceAtMost(remainingForShipModes).coerceAtLeast(
            minimumShipModeHeight
        )
        val spacerHeight = (remainingForShipModes - shipModeHeight).coerceAtLeast(0f)
        return ShipEditorLayout(
            leftColumnWidth = leftColumnWidth,
            weaponGroupsWidth = panelWidth - leftColumnWidth,
            shipPanelHeight = shipPanelHeight,
            spacerHeight = spacerHeight,
            optionsHeight = currentOptionsHeight,
            modifiersHeight = modifiersHeight,
            shipModeHeight = shipModeHeight,
        )
    }

    fun collapsedPanelHeight(): Float =
        2f * CampaignGuiStyle.PANEL_PADDING + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT

    fun shipModePanelBodyTop(): Float {
        return CampaignGuiStyle.PANEL_PADDING
    }

    fun shipModePanelBodyHeight(panelHeight: Float): Float {
        return max(0f, panelHeight - shipModePanelBodyTop() - SHIP_MODE_PANEL_BOTTOM_PADDING)
    }

    fun sharedLeftColumnWidth(panelWidth: Float): Float {
        return min(LEFT_COLUMN_WIDTH_MAX, max(LEFT_COLUMN_WIDTH_MIN, panelWidth * LEFT_COLUMN_WIDTH_FRACTION))
    }

    private fun estimateShipModePanelHeight(modes: List<String>): Float {
        val fullRows = max(1, ceil(modes.size / CampaignGuiStyle.SHIP_MODE_COLUMN_COUNT.toFloat()).toInt())
        val rows = min(SHIP_MODE_PREFERRED_VISIBLE_ROWS, fullRows)
        val totalHeight = shipModePanelHeightForBody(shipModeRowsHeight(rows))
        return max(SHIP_MODE_HEIGHT_MIN, totalHeight)
    }

    private fun minimumVisibleShipModePanelHeight(): Float {
        return shipModePanelHeightForBody(shipModeRowsHeight(SHIP_MODE_MIN_VISIBLE_ROWS))
    }

    private fun shipModePanelHeightForBody(bodyHeight: Float): Float {
        return shipModePanelBodyTop() + bodyHeight + SHIP_MODE_PANEL_BOTTOM_PADDING
    }

    private fun shipModeRowsHeight(rows: Int): Float {
        return rows * CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT +
            max(0, rows - 1) * CampaignGuiStyle.SHIP_MODE_ITEM_VGAP
    }

    private fun tightShipModePanelHeightForAvailable(modes: List<String>, availableHeight: Float): Float {
        val fullRows = max(1, ceil(modes.size / CampaignGuiStyle.SHIP_MODE_COLUMN_COUNT.toFloat()).toInt())
        val availableBodyHeight = (availableHeight - shipModePanelBodyTop() - SHIP_MODE_PANEL_BOTTOM_PADDING)
            .coerceAtLeast(0f)
        val rowStride = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT + CampaignGuiStyle.SHIP_MODE_ITEM_VGAP
        val visibleRows = floor(
            (availableBodyHeight + CampaignGuiStyle.SHIP_MODE_ITEM_VGAP) / rowStride
        ).toInt().coerceIn(1, fullRows)
        return shipModePanelHeightForBody(shipModeRowsHeight(visibleRows))
    }

    private fun heightForCollapsiblePanel(collapsed: Boolean, expandedHeight: Float): Float =
        if (collapsed) collapsedPanelHeight() else expandedHeight
}
