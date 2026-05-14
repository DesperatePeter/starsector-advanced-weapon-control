package com.dp.advancedgunnerycontrol.gui.shipview

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.input.ShipViewInputController
import com.dp.advancedgunnerycontrol.gui.panels.shipmodes.ShipModePanelRenderState
import com.dp.advancedgunnerycontrol.gui.panels.shipmodes.ShipModePanelRenderer
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

internal class ShipViewShipModePanelController(
    initialScrollOffset: Int,
    private val runtimeShip: ShipAPI?,
    private val buttons: MutableList<ButtonBase<*>>,
    private val panelPos: () -> PositionAPI?,
    private val configuredModes: (FleetMemberAPI) -> List<String>,
    private val persistenceContext: () -> ShipEditorPersistenceContext?,
    private val removePanel: (CustomPanelAPI, CustomPanelAPI, String) -> Unit,
    private val onBeforeRefresh: () -> Unit,
) {
    private var scrollOffset = initialScrollOffset.coerceAtLeast(0)
    private var state: ShipModePanelRenderState? = null

    fun captureScrollOffset(): Int = scrollOffset

    fun clearState() {
        state = null
    }

    fun build(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        panelTop: Float,
        rootHeight: Float,
    ) {
        val rendered = ShipModePanelRenderer.build(
            panel = panel,
            ship = ship,
            panelTop = panelTop,
            rootHeight = rootHeight,
            runtimeShip = runtimeShip,
            rowOffset = scrollOffset,
            configuredModes = configuredModes(ship),
            persistenceContext = persistenceContext(),
            buttons = buttons,
            onScrollRows = ::scrollByRows,
            onSelectionChanged = ::refresh,
        )
        state = rendered
        scrollOffset = rendered.effectiveScrollOffset
    }

    fun processScrollInput(events: MutableList<InputEventAPI>?): Boolean {
        return ShipViewInputController.processPanelScrollInput(
            events = events,
            panelPos = panelPos(),
            region = state?.scrollRegion,
            currentOffset = { scrollOffset },
            setOffset = { offset -> scrollOffset = offset },
            onScrolled = ::refresh,
        )
    }

    fun refresh() {
        onBeforeRefresh()
        val current = state ?: return
        val rendered = ShipModePanelRenderer.refresh(
            state = current,
            runtimeShip = runtimeShip,
            rowOffset = scrollOffset,
            configuredModes = configuredModes(current.ship),
            persistenceContext = persistenceContext(),
            buttons = buttons,
            removePanel = { parent, child -> removePanel(parent, child, "ship-mode rows") },
            onScrollRows = ::scrollByRows,
            onSelectionChanged = ::refresh,
        )
        state = rendered
        scrollOffset = rendered.effectiveScrollOffset
    }

    private fun scrollByRows(delta: Int) {
        ShipViewInputController.scrollByRows(
            currentOffset = scrollOffset,
            delta = delta,
            maxOffset = state?.maxScrollOffset ?: 0,
            setOffset = { offset -> scrollOffset = offset },
            onScrolled = ::refresh,
        )
    }
}
