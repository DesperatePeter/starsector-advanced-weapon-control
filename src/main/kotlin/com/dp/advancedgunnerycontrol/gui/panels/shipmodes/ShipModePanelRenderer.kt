package com.dp.advancedgunnerycontrol.gui.panels.shipmodes

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.shipmodes.ShipModeButton

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.layout.ShipEditorLayoutCalculator

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

internal data class ShipModePanelRenderState(
    val parentPanel: CustomPanelAPI,
    val itemPanel: CustomPanelAPI,
    val ship: FleetMemberAPI,
    val innerWidth: Float,
    val bodyTop: Float,
    val bodyHeight: Float,
    val panelTop: Float,
    val rootHeight: Float,
    val controls: List<ButtonBase<*>>,
    val scrollRegion: VerticalScrollRegion<Unit>,
    val maxScrollOffset: Int,
    val effectiveScrollOffset: Int,
)

internal object ShipModePanelRenderer {
    fun build(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        panelTop: Float,
        rootHeight: Float,
        runtimeShip: ShipAPI?,
        rowOffset: Int,
        configuredModes: List<String>,
        persistenceContext: ShipEditorPersistenceContext?,
        buttons: MutableList<ButtonBase<*>>,
        onScrollRows: (Int) -> Unit,
        onSelectionChanged: () -> Unit,
    ): ShipModePanelRenderState {
        val bodyTop = ShipEditorLayoutCalculator.shipModePanelBodyTop()
        return renderRows(
            parentPanel = panel,
            ship = ship,
            innerWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING,
            bodyTop = bodyTop,
            bodyHeight = ShipEditorLayoutCalculator.shipModePanelBodyHeight(panel.position.height),
            panelTop = panelTop,
            rootHeight = rootHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            configuredModes = configuredModes,
            persistenceContext = persistenceContext,
            forceActiveSectionExpanded = true,
            buttons = buttons,
            onScrollRows = onScrollRows,
            onSelectionChanged = onSelectionChanged,
        )
    }

    fun refresh(
        state: ShipModePanelRenderState,
        runtimeShip: ShipAPI?,
        rowOffset: Int,
        configuredModes: List<String>,
        persistenceContext: ShipEditorPersistenceContext?,
        buttons: MutableList<ButtonBase<*>>,
        removePanel: (CustomPanelAPI, CustomPanelAPI) -> Unit,
        onScrollRows: (Int) -> Unit,
        onSelectionChanged: () -> Unit,
    ): ShipModePanelRenderState {
        removePanel(state.parentPanel, state.itemPanel)
        buttons.removeAll(state.controls.toSet())
        return renderRows(
            parentPanel = state.parentPanel,
            ship = state.ship,
            innerWidth = state.innerWidth,
            bodyTop = state.bodyTop,
            bodyHeight = state.bodyHeight,
            panelTop = state.panelTop,
            rootHeight = state.rootHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            configuredModes = configuredModes,
            persistenceContext = persistenceContext,
            forceActiveSectionExpanded = false,
            buttons = buttons,
            onScrollRows = onScrollRows,
            onSelectionChanged = onSelectionChanged,
        )
    }

    private fun renderRows(
        parentPanel: CustomPanelAPI,
        ship: FleetMemberAPI,
        innerWidth: Float,
        bodyTop: Float,
        bodyHeight: Float,
        panelTop: Float,
        rootHeight: Float,
        runtimeShip: ShipAPI?,
        rowOffset: Int,
        configuredModes: List<String>,
        persistenceContext: ShipEditorPersistenceContext?,
        forceActiveSectionExpanded: Boolean,
        buttons: MutableList<ButtonBase<*>>,
        onScrollRows: (Int) -> Unit,
        onSelectionChanged: () -> Unit,
    ): ShipModePanelRenderState {
        val preparedModeGroup = ShipModeButton.prepareCampaignModeButtonGroup(
            ship = ship,
            width = innerWidth,
            availableHeight = bodyHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            configuredModesOverride = configuredModes,
            persistenceContext = persistenceContext,
            forceActiveSectionExpanded = forceActiveSectionExpanded,
        )
        val itemPanelHeight = ShipModeButton.estimateCampaignModeButtonGroupTightHeight(
            ship = ship,
            width = innerWidth,
            availableHeight = bodyHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            preparedGroup = preparedModeGroup,
        )
        val itemPanel = parentPanel.createCustomPanel(innerWidth, itemPanelHeight, null)
        parentPanel.addComponent(itemPanel)
        itemPanel.position.inTL(CampaignGuiStyle.PANEL_PADDING, bodyTop)
        val rendered = ShipModeButton.createCampaignModeButtonGroup(
            ship = ship,
            panel = itemPanel,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            onScrollRows = onScrollRows,
            layoutContainerHeight = bodyHeight,
            onSelectionChanged = onSelectionChanged,
            preparedGroup = preparedModeGroup,
            persistenceContext = persistenceContext,
        )
        val maxScrollOffset = rendered.maxRowOffset
        val effectiveScrollOffset = rendered.effectiveRowOffset.coerceIn(0, maxScrollOffset)
        buttons.addAll(rendered.buttons)
        return ShipModePanelRenderState(
            parentPanel = parentPanel,
            itemPanel = itemPanel,
            ship = ship,
            innerWidth = innerWidth,
            bodyTop = bodyTop,
            bodyHeight = bodyHeight,
            panelTop = panelTop,
            rootHeight = rootHeight,
            controls = rendered.buttons,
            scrollRegion = VerticalScrollRegion(
                key = Unit,
                left = CampaignGuiStyle.PANEL_PADDING,
                right = CampaignGuiStyle.PANEL_PADDING + innerWidth,
                bottom = rootHeight - panelTop - bodyTop - itemPanelHeight,
                top = rootHeight - panelTop - bodyTop,
                maxOffset = maxScrollOffset,
            ),
            maxScrollOffset = maxScrollOffset,
            effectiveScrollOffset = effectiveScrollOffset,
        )
    }
}
