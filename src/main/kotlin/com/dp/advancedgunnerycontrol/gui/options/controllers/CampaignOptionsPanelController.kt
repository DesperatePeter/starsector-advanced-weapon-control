package com.dp.advancedgunnerycontrol.gui.options.controllers

import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.actions.ResetAction
import com.dp.advancedgunnerycontrol.gui.actions.shipActionRequiresConfirmation
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

class CampaignOptionsPanelController(
    private val buttonListenerProvider: () -> CustomPanelAPI?,
    private val executeAction: (GUIAction) -> Unit,
    private val requestRebuild: () -> Unit,
    private val requestConfirmationRefresh: () -> Unit = requestRebuild,
    private val capabilities: ShipEditorCapabilities = ShipEditorCapabilities.CAMPAIGN,
    private val afterResetAction: () -> Unit = {},
    private val extraRowsProvider: () -> List<CampaignOptionRow> = { emptyList() },
) {
    private var currentActions: List<GUIAction> = emptyList()
    private val panelSupport = CampaignOptionPanelControllerSupport(
        capabilities = capabilities,
        requestRebuild = requestRebuild,
        requestConfirmationRefresh = requestConfirmationRefresh,
        extraRowsProvider = extraRowsProvider,
        bindListener = { button ->
            CampaignButtonSuppression.bindCampaignButtonListener(button, buttonListenerProvider(), "Bind AGC campaign action button")
        },
    )

    fun updateActions(actions: List<GUIAction>) {
        currentActions = actions
        panelSupport.updateRows(buildRows())
    }

    fun consumeModifierChange(): Boolean {
        return panelSupport.consumeModifierChange()
    }

    fun advance() {
        panelSupport.advanceButtons()
    }

    fun handleButtonId(buttonId: Any?): Boolean {
        return panelSupport.handleButtonId(buttonId, ::executeRow)
    }

    fun handleShortcut(shortcut: Int): Boolean {
        return panelSupport.handleShortcut(shortcut, ::executeRow)
    }

    fun processInput(events: MutableList<InputEventAPI>?): Boolean {
        return panelSupport.processInput(events)
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

    private fun buildRows(): List<CampaignOptionRow> {
        return buildRows(useStableLabels = false)
    }

    private fun buildRows(useStableLabels: Boolean): List<CampaignOptionRow> {
        val modifiers = GUIAction.modifierKeys()
        return buildCampaignOptionRowsFromActions(
            actions = currentActions,
            useStableLabels = useStableLabels,
            confirmationKey = { action ->
                if (shipActionRequiresConfirmation(action)) {
                    campaignConfirmationKey(action, modifiers)
                } else {
                    null
                }
            },
            rightClickCallback = ::rightClickActionCallback,
            rebuildAfter = { action -> shipActionRequiresConfirmation(action) },
            callback = { action ->
                {
                    if (shipActionRequiresConfirmation(action)) {
                        executeConfirmedAction(action, modifiers)
                    } else {
                        executeAction(action)
                    }
                }
            }
        )
    }

    private fun rightClickActionCallback(action: GUIAction): (() -> Unit)? {
        if (!action.supportsRightClick()) return null
        return {
            if (action.executeRightClick()) {
                requestRebuild()
            }
        }
    }

    private fun campaignConfirmationKey(
        action: GUIAction,
        modifiers: Pair<Boolean, Boolean>
    ): String {
        return "${action::class.java.name}:${modifiers.first}:${modifiers.second}"
    }

    private fun executeConfirmedAction(action: GUIAction, modifiers: Pair<Boolean, Boolean>) {
        when (action) {
            is ResetAction -> {
                action.executeWithModifiers(modifiers.first, modifiers.second)
                afterResetAction()
            }
            else -> executeAction(action)
        }
    }

    private fun executeRow(row: CampaignOptionRow) {
        row.callback()
        panelSupport.finishRowExecution(row)
    }
}
