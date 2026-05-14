package com.dp.advancedgunnerycontrol.gui.options.controllers

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult
import com.dp.advancedgunnerycontrol.gui.session.ShipViewHotTagCache

import com.dp.advancedgunnerycontrol.gui.actions.CycleLoadoutAction
import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.actions.GoToSuggestedTagsAction
import com.dp.advancedgunnerycontrol.gui.actions.NextShipAction
import com.dp.advancedgunnerycontrol.gui.actions.ReloadSettingsAction
import com.dp.advancedgunnerycontrol.gui.actions.ResetAction
import com.dp.advancedgunnerycontrol.gui.actions.ResetCustomTagListAction
import com.dp.advancedgunnerycontrol.gui.actions.ToggleSimpleAdvancedAction
import com.dp.advancedgunnerycontrol.gui.actions.generateShipActions
import com.dp.advancedgunnerycontrol.gui.actions.shipActionRequiresConfirmation
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.reloadShips
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import org.lwjgl.input.Keyboard

class DirectShipEditorOptionsController(
    private val activeMemberProvider: () -> FleetMemberAPI?,
    private val activeRuntimeShipProvider: () -> ShipAPI?,
    private val selectAdjacentShip: (previous: Boolean) -> Unit,
    private val tagView: TagListView,
    private val requestRebuild: () -> Unit,
    private val requestConfirmationRefresh: () -> Unit = requestRebuild,
    private val openSuggestedTags: () -> Unit,
    private val capabilities: ShipEditorCapabilities = ShipEditorCapabilities.DIRECT,
    private val afterResetAction: () -> Unit = {},
    private val extraRowsProvider: () -> List<CampaignOptionRow> = { emptyList() },
) {
    companion object {
        private const val OPTIONS_MIN_HEIGHT = 120f
        private const val RESET_CONFIRMATION_KEY = "reset_current_ship"
        private const val RELOAD_SETTINGS_CONFIRMATION_KEY = "reload_settings"
        private const val RESET_CUSTOM_TAGS_CONFIRMATION_KEY = "reset_custom_tags"
    }

    private val actionAttributes = GUIAttributes()
    private val panelSupport = CampaignOptionPanelControllerSupport(
        capabilities = capabilities,
        requestRebuild = requestRebuild,
        requestConfirmationRefresh = requestConfirmationRefresh,
        extraRowsProvider = extraRowsProvider,
        minHeight = OPTIONS_MIN_HEIGHT,
    )

    fun rebuildRows() {
        panelSupport.updateRows(buildBaseRows(useStableLabels = false))
    }

    fun advance() {
        panelSupport.advanceButtons()
    }

    fun processInput(events: MutableList<InputEventAPI>?): Boolean {
        return panelSupport.processInput(events)
    }

    fun consumeModifierChange(): Boolean {
        return panelSupport.consumeModifierChange()
    }

    fun handleShortcut(keyCode: Int): Boolean {
        return panelSupport.handleShortcut(keyCode, ::executeRow)
    }

    fun buildPanel(
        panel: CustomPanelAPI,
        renderHeading: Boolean,
        rowOffset: Int,
        visibleBodyHeight: Float?,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): CampaignOptionsRenderResult {
        return panelSupport.buildPanel(
            panel = panel,
            executeRow = ::executeRow,
            renderHeading = renderHeading,
            rowOffset = rowOffset,
            visibleBodyHeight = visibleBodyHeight,
            onScrollIndicator = onScrollIndicator,
        )
    }

    fun buildModifiersPanel(panel: CustomPanelAPI) {
        panelSupport.buildModifiersPanel(panel)
    }

    fun suppressButtonHover() {
        panelSupport.suppressButtonHover()
    }

    fun restoreButtonHover() {
        panelSupport.restoreButtonHover()
    }

    fun confirmationRequest(): CampaignConfirmationModalRequest? {
        return panelSupport.confirmationRequest()
    }

    fun estimateHeight(width: Float): Float {
        return panelSupport.estimateHeight(width)
    }

    fun estimateStableHeight(width: Float): Float {
        return panelSupport.estimateStableHeight(width)
    }

    fun estimateModifiersHeight(): Float {
        return panelSupport.estimateModifiersHeight()
    }

    fun customListStateAttributes(): GUIAttributes {
        return actionAttributes
    }

    private fun executeRow(row: CampaignOptionRow) {
        row.callback()
        panelSupport.finishRowExecution(row)
    }

    private fun buildBaseRows(useStableLabels: Boolean): List<CampaignOptionRow> {
        return buildRowsFromGeneratedActions(useStableLabels)
    }

    private fun buildRowsFromGeneratedActions(useStableLabels: Boolean): List<CampaignOptionRow> {
        val attributes = actionAttributes()
        val modifiers = GUIAction.modifierKeys()
        return buildCampaignOptionRowsFromActions(
            actions = generateShipActions(attributes, capabilities),
            useStableLabels = useStableLabels,
            confirmationKey = ::directConfirmationKey,
            rightClickCallback = ::rightClickActionCallback,
            callback = { action ->
                {
                    executeDirectAction(
                        action = action,
                        allLoadouts = modifiers.first,
                        wholeFleet = modifiers.second
                    )
                }
            }
        )
    }

    private fun executeDirectAction(
        action: GUIAction,
        allLoadouts: Boolean,
        wholeFleet: Boolean,
        rightClick: Boolean = false,
    ) {
        if (rightClick) {
            when (action) {
                is NextShipAction -> selectNextOrPreviousShip(rightClick = true)
                is CycleLoadoutAction -> cycleLoadout(-1)
                else -> {
                    if (action.executeRightClick()) {
                        tagView.reset()
                    }
                }
            }
            return
        }
        when (action) {
            is NextShipAction -> selectNextOrPreviousShip()
            is CycleLoadoutAction -> cycleLoadout(1)
            is ResetAction -> resetCurrentShip(allLoadouts, wholeFleet)
            is ReloadSettingsAction -> reloadSettings()
            is GoToSuggestedTagsAction -> openSuggestedTags()
            is ToggleSimpleAdvancedAction -> {
                action.execute()
                tagView.reset()
            }
            else -> {
                action.execute()
                tagView.reset()
            }
        }
    }

    private fun rightClickActionCallback(action: GUIAction): (() -> Unit)? {
        if (!action.supportsRightClick()) return null
        return {
            executeDirectAction(
                action = action,
                allLoadouts = GUIAction.isAllLoadoutsKeyHeld(),
                wholeFleet = GUIAction.isWholeFleetKeyHeld(),
                rightClick = true
            )
            requestRebuild()
        }
    }

    private fun directConfirmationKey(action: GUIAction): String? {
        if (!shipActionRequiresConfirmation(action)) return null
        return when (action) {
            is ResetAction -> {
                val modifiers = GUIAction.modifierKeys()
                "$RESET_CONFIRMATION_KEY:${modifiers.first}:${modifiers.second}"
            }
            is ReloadSettingsAction -> RELOAD_SETTINGS_CONFIRMATION_KEY
            is ResetCustomTagListAction -> RESET_CUSTOM_TAGS_CONFIRMATION_KEY
            else -> "${action::class.java.name}:${action.getName()}"
        }
    }

    private fun actionAttributes(): GUIAttributes {
        return actionAttributes.also { attributes ->
            attributes.ship = activeMemberProvider()
            attributes.runtimeShip = activeRuntimeShipProvider()
            attributes.tagView = tagView
        }
    }

    private fun cycleLoadout(delta: Int = 1) {
        if (delta < 0) {
            CycleLoadoutAction(actionAttributes()).executeRightClick()
        } else {
            CycleLoadoutAction(actionAttributes()).execute()
        }
        reloadActiveRuntimeShip()
    }

    private fun reloadActiveRuntimeShip(loadoutIndex: Int = AGCGUI.storageIndex) {
        activeRuntimeShipProvider()?.let { runtimeShip ->
            reloadShips(loadoutIndex, listOf(runtimeShip))
        }
        tagView.reset()
    }

    private fun resetCurrentShip(allLoadouts: Boolean, wholeFleet: Boolean) {
        ResetAction(actionAttributes()).executeWithModifiers(allLoadouts, wholeFleet)
        reloadActiveRuntimeShip()
        afterResetAction()
    }

    private fun reloadSettings() {
        Settings.loadSettings()
        ShipViewHotTagCache.invalidate()
        tagView.reset()
    }

    private fun selectNextOrPreviousShip(rightClick: Boolean = false) {
        val shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
        selectAdjacentShip(if (rightClick) !shiftHeld else shiftHeld)
    }
}
