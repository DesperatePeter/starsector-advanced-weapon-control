package com.dp.advancedgunnerycontrol.gui.controls.shipmodes

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.LegacyAgcTooltipCheckbox
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.scroll.CampaignTagScrollButtons
import com.dp.advancedgunnerycontrol.gui.controls.scroll.visibleScrollPageDelta
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleHeading
import com.dp.advancedgunnerycontrol.gui.controls.weapontags.CampaignTagToggleControls
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.shipmodes.defaultShipMode
import com.dp.advancedgunnerycontrol.shipmodes.shipModeDescription
import com.dp.advancedgunnerycontrol.shipmodes.shipModeDisplayName
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI

internal object ShipModeButtonFactory {
    fun createModeButtonGroup(
        ship: FleetMemberAPI,
        panel: CustomPanelAPI,
        position: UIComponentAPI,
    ): List<ShipModeButton> {
        val modeButtons = mutableListOf<ShipModeButton>()
        val elementList = mutableListOf<TooltipMakerAPI>()
        Settings.getCurrentShipModeNames().forEach { configuredMode ->
            val modeName = configuredMode.ifBlank { defaultShipMode }
            val tooltip = panel.createUIElement(130f, 30f, false)
            modeButtons.add(
                ShipModeButton(
                    ship, modeName, LegacyAgcTooltipCheckbox.add(
                        tooltip = tooltip,
                        label = shipModeDisplayName(modeName),
                        data = modeName,
                        tooltipText = shipModeDescription(modeName),
                        width = 130f
                    )
                )
            )
            if (elementList.isEmpty()) {
                panel.addUIElement(tooltip).belowLeft(position, 5f)
            } else {
                panel.addUIElement(tooltip).rightOfTop(elementList.last(), 12f)
            }
            elementList.add(tooltip)
        }

        modeButtons.forEach {
            it.connectSameGroupButtons(modeButtons)
        }
        ShipModeSelectionStore.applyPersistedModes(modeButtons, ship, null)
        return modeButtons
    }

    fun createCampaignModeButtonGroup(
        ship: FleetMemberAPI,
        panel: CustomPanelAPI,
        runtimeShip: ShipAPI? = null,
        rowOffset: Int = 0,
        onScrollRows: (Int) -> Unit = {},
        layoutContainerHeight: Float = panel.position.height,
        onSelectionChanged: (() -> Unit)? = null,
        preparedGroup: PreparedCampaignModeButtonGroup? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): CampaignModeButtonGroupResult {
        val prepared = preparedGroup ?: ShipModeButtonPlanner.prepareCampaignModeButtonGroup(
            ship = ship,
            width = panel.position.width,
            availableHeight = layoutContainerHeight,
            runtimeShip = runtimeShip,
            rowOffset = rowOffset,
            persistenceContext = persistenceContext,
        )
        val persistedModeList = prepared.persistedModeList
        val persistedModes = persistedModeList.toSet()
        val plan = prepared.plan
        val layout = plan.layout
        val modeButtons = mutableListOf<ShipModeButton>()
        val allButtons = mutableListOf<ButtonBase<*>>()

        var nextTop = 0f
        val pageDelta = visibleScrollPageDelta(layout.scrollSlice)

        if (layout.scrollSlice.hasAbove) {
            allButtons.add(renderCampaignModeScrollIndicator(
                panel = panel,
                top = nextTop,
                symbol = CampaignGuiStyle.SCROLL_INDICATOR_ABOVE,
                onClick = { onScrollRows(-pageDelta) },
            ))
            nextTop += CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT + CampaignGuiStyle.SHIP_MODE_ITEM_VGAP
        }

        renderCampaignModeRows(
            ship = ship,
            panel = panel,
            runtimeShip = runtimeShip,
            persistenceContext = persistenceContext,
            plan = plan,
            rows = layout.scrollSlice.items,
            top = nextTop,
            persistedModeList = persistedModeList,
            onSelectionChanged = onSelectionChanged,
            modeButtons = modeButtons,
            allButtons = allButtons,
        )
        if (layout.scrollSlice.items.isNotEmpty()) {
            nextTop += layout.renderedScrollItemsHeight
        }
        allButtons.addAll(modeButtons)

        if (layout.scrollSlice.hasBelow) {
            if (layout.scrollSlice.items.isNotEmpty()) {
                nextTop += CampaignGuiStyle.SHIP_MODE_ITEM_VGAP
            }
            allButtons.add(renderCampaignModeScrollIndicator(
                panel = panel,
                top = nextTop,
                symbol = CampaignGuiStyle.SCROLL_INDICATOR_BELOW,
                onClick = { onScrollRows(pageDelta) },
            ))
        }

        modeButtons.forEach {
            it.connectSameGroupButtons(modeButtons)
        }
        ShipModeButton.updateButtonsFromModes(modeButtons, persistedModes.toList())
        return CampaignModeButtonGroupResult(allButtons, layout.scrollSlice.maxOffset, layout.scrollSlice.offset)
    }

    private fun renderCampaignModeRows(
        ship: FleetMemberAPI,
        panel: CustomPanelAPI,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
        plan: CampaignModeButtonGroupPlan,
        rows: List<CampaignModeListRow>,
        top: Float,
        persistedModeList: List<String>,
        onSelectionChanged: (() -> Unit)?,
        modeButtons: MutableList<ShipModeButton>,
        allButtons: MutableList<ButtonBase<*>>,
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val rowTop = top + rowIndex * (CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT + CampaignGuiStyle.SHIP_MODE_ITEM_VGAP)
            when (row) {
                is CampaignModeListRow.Heading -> {
                    allButtons.add(renderCampaignModeSectionHeading(panel, plan.sectionKey, row, rowTop, onSelectionChanged))
                }
                is CampaignModeListRow.Modes -> renderCampaignModeButtonRow(
                    ship = ship,
                    panel = panel,
                    runtimeShip = runtimeShip,
                    persistenceContext = persistenceContext,
                    plan = plan,
                    row = row,
                    rowTop = rowTop,
                    persistedModeList = persistedModeList,
                    onSelectionChanged = onSelectionChanged,
                    modeButtons = modeButtons,
                )
            }
        }
    }

    private fun renderCampaignModeSectionHeading(
        panel: CustomPanelAPI,
        sectionKey: String,
        row: CampaignModeListRow.Heading,
        top: Float,
        onSelectionChanged: (() -> Unit)?,
    ): ButtonBase<*> {
        val heading = CampaignToggleHeading(
            title = row.title,
            expanded = row.expanded,
            active = row.active,
            subject = "section",
            inactiveKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
            activeKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
        )
        return CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = panel,
            data = "ship_mode_section:$sectionKey:${row.title}",
            x = 0f,
            y = top,
            width = panel.position.width,
            height = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
            template = CampaignGuiStyle.actionButtonTemplate(heading.kind),
            labelText = heading.label,
            tooltip = heading.tooltip,
            centerText = true,
        ) {
            ShipModeButtonPlanner.toggleSection(sectionKey, row.title, row.expanded)
            onSelectionChanged?.invoke() ?: ShipModeButton.notifyCampaignShipModeSelectionChanged()
        }
    }

    private fun renderCampaignModeButtonRow(
        ship: FleetMemberAPI,
        panel: CustomPanelAPI,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
        plan: CampaignModeButtonGroupPlan,
        row: CampaignModeListRow.Modes,
        rowTop: Float,
        persistedModeList: List<String>,
        onSelectionChanged: (() -> Unit)?,
        modeButtons: MutableList<ShipModeButton>,
    ) {
        row.modes.forEachIndexed { columnIndex, modeName ->
            renderCampaignModeButton(
                ship = ship,
                panel = panel,
                runtimeShip = runtimeShip,
                persistenceContext = persistenceContext,
                plan = plan,
                columnIndex = columnIndex,
                rowTop = rowTop,
                modeName = modeName,
                persistedModeList = persistedModeList,
                onSelectionChanged = onSelectionChanged,
                modeButtons = modeButtons,
            )
        }
    }

    private fun renderCampaignModeButton(
        ship: FleetMemberAPI,
        panel: CustomPanelAPI,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext?,
        plan: CampaignModeButtonGroupPlan,
        columnIndex: Int,
        rowTop: Float,
        modeName: String,
        persistedModeList: List<String>,
        onSelectionChanged: (() -> Unit)?,
        modeButtons: MutableList<ShipModeButton>,
    ) {
        val itemWidth = plan.itemWidth
        val shell = CampaignTagToggleControls.addTagModeToggleShell(
            parent = panel,
            data = modeName,
            x = columnIndex * (itemWidth + CampaignGuiStyle.SHIP_MODE_ITEM_HGAP),
            y = rowTop,
            width = itemWidth,
            height = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
            tooltip = shipModeDescription(modeName),
            fillIdle = modeName !in plan.activeModeSet,
            fillSelected = modeName in plan.activeModeSet
        )
        modeButtons.add(
            ShipModeButton(
                ship,
                modeName,
                shell.button,
                runtimeShip,
                onSelectionChanged,
                persistedModeList,
                persistenceContext,
                useMomentaryVisualState = true,
            )
        )
        modeButtons.last().updateCheckedFromModes(persistedModeList)
        CampaignControlLabels.renderCenteredControlLabel(
            panel = shell.panel,
            text = shipModeDisplayName(modeName),
            width = itemWidth,
            height = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
            centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET,
            centerRegionWidth = itemWidth,
            horizontalPadding = 0f,
            approxCharWidthPx = CampaignGuiStyle.SHIP_MODE_LABEL_CHAR_WIDTH_ESTIMATE
        )
    }

    private fun renderCampaignModeScrollIndicator(
        panel: CustomPanelAPI,
        top: Float,
        symbol: String,
        onClick: () -> Unit,
    ): ButtonBase<*> {
        val (_, button) = CampaignTagScrollButtons.addTagScrollIndicatorMomentaryButton(
            parent = panel,
            top = top,
            symbol = symbol,
            data = "ship_modes_$symbol",
            height = CampaignGuiStyle.SHIP_MODE_ITEM_HEIGHT,
            callback = onClick,
        )
        return button
    }
}
