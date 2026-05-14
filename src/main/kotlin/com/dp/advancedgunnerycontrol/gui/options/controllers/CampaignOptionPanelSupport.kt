package com.dp.advancedgunnerycontrol.gui.options.controllers

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonCallbackPoller
import com.dp.advancedgunnerycontrol.gui.controls.confirmations.PendingConfirmationState
import com.dp.advancedgunnerycontrol.gui.controls.panels.CampaignModifiersPanel
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppressionSnapshot
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult
import com.dp.advancedgunnerycontrol.gui.options.rendering.CampaignOptionLayoutCache
import com.dp.advancedgunnerycontrol.gui.options.rendering.CampaignOptionPanelLayout
import com.dp.advancedgunnerycontrol.gui.options.rendering.CampaignOptionPanelRenderer

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.actions.shipActionButtonKind
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

/**
 * Shared Options panel controller component.
 * Owns option-row state, confirmation arming, right-click handling, shortcut
 * dispatch, and button polling for campaign and direct/refit hosts.
 */
class CampaignOptionPanelControllerSupport(
    private val capabilities: ShipEditorCapabilities,
    private val requestRebuild: () -> Unit,
    private val requestConfirmationRefresh: () -> Unit = requestRebuild,
    private val extraRowsProvider: () -> List<CampaignOptionRow> = { emptyList() },
    private val minHeight: Float = 0f,
    private val bindListener: ((ButtonAPI) -> Unit)? = null,
    private val afterRightClick: (CampaignOptionRow) -> Unit = { row ->
        if (row.rebuildAfter) requestRebuild()
    },
) {
    private val optionButtons = mutableListOf<ButtonBase<*>>()
    private val buttonCallbackPoller = CampaignButtonCallbackPoller(optionButtons)
    private var lastModifierKeys = GUIAction.modifierKeys()
    private var currentBaseRows: List<CampaignOptionRow> = emptyList()
    private var currentRows: List<CampaignOptionRow> = emptyList()
    private val confirmationController = CampaignOptionConfirmationController(requestRebuild)
    private val layoutCache = CampaignOptionLayoutCache()
    private var suppressionSnapshot: CampaignButtonSuppressionSnapshot? = null

    fun updateRows(baseRows: List<CampaignOptionRow>) {
        layoutCache.clear()
        currentBaseRows = baseRows
        confirmationController.clearIfUnavailable(baseRows)
        currentRows = confirmationController.armRows(baseRows)
    }

    fun advanceButtons() {
        buttonCallbackPoller.processIfRequested()
    }

    fun consumeModifierChange(): Boolean {
        val currentModifierKeys = GUIAction.modifierKeys()
        if (lastModifierKeys == currentModifierKeys) return false
        lastModifierKeys = currentModifierKeys
        return true
    }

    fun processInput(events: MutableList<InputEventAPI>?): Boolean {
        buttonCallbackPoller.requestPollFromInput(events)
        return optionButtons.processCampaignOptionButtonInput(events)
    }

    fun finishRowExecution(row: CampaignOptionRow) {
        if (row.confirmationKey != null) {
            requestConfirmationRefresh()
        } else if (row.rebuildAfter) {
            requestRebuild()
        }
    }

    fun handleButtonId(
        buttonId: Any?,
        executeRow: (CampaignOptionRow) -> Unit,
    ): Boolean {
        return executeCampaignOptionButtonId(buttonId, executeRow)
    }

    fun handleShortcut(
        shortcut: Int,
        executeRow: (CampaignOptionRow) -> Unit,
    ): Boolean {
        val row = findCampaignOptionShortcutRow(currentRows, extraRowsProvider, shortcut) ?: return false
        executeRow(row)
        return true
    }

    fun buildPanel(
        panel: CustomPanelAPI,
        executeRow: (CampaignOptionRow) -> Unit,
        renderHeading: Boolean = true,
        rowOffset: Int = 0,
        visibleBodyHeight: Float? = null,
        onScrollIndicator: (delta: Int, maxOffset: Int) -> Unit,
    ): CampaignOptionsRenderResult {
        suppressionSnapshot = null
        val rows = CampaignOptionPanelLayout.campaignOptionRowsWithExtra(currentRows, extraRowsProvider())
        return CampaignOptionPanelRenderer.renderBoundCampaignOptionsPanel(
            panel = panel,
            layout = layoutCache.layout(width = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING, rows = rows),
            optionButtons = optionButtons,
            executeRow = executeRow,
            renderHeading = renderHeading,
            rowOffset = rowOffset,
            visibleBodyHeight = visibleBodyHeight,
            onScrollIndicator = onScrollIndicator,
            bindListener = bindListener,
            afterRightClick = afterRightClick,
        )
    }

    fun confirmationRequest(): CampaignConfirmationModalRequest? {
        return confirmationController.request(currentBaseRows)
    }

    fun buildModifiersPanel(panel: CustomPanelAPI) {
        buildCampaignOptionModifiersPanel(panel, capabilities)
    }

    fun suppressButtonHover() {
        if (suppressionSnapshot == null) {
            suppressionSnapshot = optionButtons.suppressCampaignOptionButtonHover()
        }
    }

    fun restoreButtonHover() {
        suppressionSnapshot?.restore()
        suppressionSnapshot = null
    }

    fun estimateHeight(width: Float): Float {
        return layoutCache.layout(
            width = width,
            rows = CampaignOptionPanelLayout.campaignOptionRowsWithExtra(currentRows, extraRowsProvider()),
            minHeight = minHeight
        ).requiredHeight
    }

    fun estimateStableHeight(width: Float): Float {
        return layoutCache.layout(
            width = width,
            rows = CampaignOptionPanelLayout.stableAdvancedOptionRows(capabilities),
            minHeight = minHeight
        ).requiredHeight
    }

    fun estimateModifiersHeight(): Float {
        return estimateCampaignOptionModifiersHeight(capabilities)
    }
}

class CampaignOptionConfirmationController(
    private val requestRebuild: () -> Unit,
) {
    private val pendingConfirmation = PendingConfirmationState<String>()

    fun clearIfUnavailable(baseRows: List<CampaignOptionRow>) {
        pendingConfirmation.clearIfUnavailable(baseRows.mapNotNull { it.confirmationKey })
    }

    fun armRows(baseRows: List<CampaignOptionRow>): List<CampaignOptionRow> {
        return baseRows.map { row ->
            val key = row.confirmationKey ?: return@map row
            row.copy(callback = { pendingConfirmation.arm(key) })
        }
    }

    fun request(baseRows: List<CampaignOptionRow>): CampaignConfirmationModalRequest? {
        val pendingKey = pendingConfirmation.key ?: return null
        val row = baseRows.firstOrNull { it.confirmationKey == pendingKey } ?: return null
        return campaignConfirmationRequestWithFooter(
            title = row.confirmationTitle ?: "Confirm ${row.label}",
            description = row.confirmationDescription ?: "Confirming will execute ${row.label}.",
            tone = row.confirmationTone,
            onConfirm = {
                row.callback()
                pendingConfirmation.clear()
                if (row.rebuildAfter) {
                    requestRebuild()
                }
            },
            onCancel = {
                pendingConfirmation.clear()
            }
        )
    }
}

fun campaignOptionRowForAction(
    action: GUIAction,
    useStableLabel: Boolean = false,
    kind: CampaignActionButtonKind? = null,
    rebuildAfter: Boolean = true,
    confirmationKey: String? = null,
    rightClickCallback: (() -> Unit)? = null,
    callback: () -> Unit,
): CampaignOptionRow {
    return CampaignOptionRow(
        label = if (useStableLabel) action.getStableLayoutName() else action.getName(),
        tooltip = action.getTooltip(),
        shortcut = action.getDisplayShortcut(),
        activationShortcut = action.getShortcut(),
        kind = kind ?: shipActionButtonKind(action),
        rebuildAfter = rebuildAfter,
        confirmationKey = confirmationKey,
        confirmationTitle = action.getConfirmationTitle(),
        confirmationDescription = action.getConfirmationDescription(),
        confirmationTone = action.getConfirmationTone(),
        rightClickCallback = rightClickCallback,
        callback = callback
    )
}

fun buildCampaignOptionRowsFromActions(
    actions: List<GUIAction>,
    useStableLabels: Boolean,
    confirmationKey: (GUIAction) -> String? = { null },
    rightClickCallback: (GUIAction) -> (() -> Unit)? = { null },
    rebuildAfter: (GUIAction) -> Boolean = { true },
    callback: (GUIAction) -> () -> Unit,
): List<CampaignOptionRow> {
    return actions.map { action ->
        campaignOptionRowForAction(
            action = action,
            useStableLabel = useStableLabels,
            confirmationKey = confirmationKey(action),
            rightClickCallback = rightClickCallback(action),
            rebuildAfter = rebuildAfter(action),
            callback = callback(action),
        )
    }
}

fun MutableList<ButtonBase<*>>.bindCampaignOptionRowButton(
    row: CampaignOptionRow,
    button: ButtonAPI,
    executeRow: (CampaignOptionRow) -> Unit,
    afterRightClick: (CampaignOptionRow) -> Unit = {},
): ButtonBase<*> {
    val control = CampaignButtonControls.addControl(this, button = button) { executeRow(row) }
    row.rightClickCallback?.let { callback ->
        control.onRightClick {
            callback()
            afterRightClick(row)
        }
    }
    button.setShowTooltipWhileInactive(true)
    return control
}

fun MutableList<ButtonBase<*>>.processCampaignOptionButtonInput(events: MutableList<InputEventAPI>?): Boolean {
    return CampaignButtonControls.processRightClickInput(this, events)
}

fun MutableList<ButtonBase<*>>.suppressCampaignOptionButtonHover(): CampaignButtonSuppressionSnapshot {
    return CampaignButtonSuppression.suppressControlHoverSnapshot(this)
}

fun findCampaignOptionShortcutRow(
    rows: List<CampaignOptionRow>,
    extraRowsProvider: () -> List<CampaignOptionRow>,
    shortcut: Int,
): CampaignOptionRow? {
    return CampaignOptionPanelLayout.campaignOptionRowsWithExtra(rows, extraRowsProvider()).firstOrNull { it.activationShortcut == shortcut }
}

fun executeCampaignOptionButtonId(
    buttonId: Any?,
    executeRow: (CampaignOptionRow) -> Unit,
): Boolean {
    val row = buttonId as? CampaignOptionRow ?: return false
    executeRow(row)
    return true
}

fun buildCampaignOptionModifiersPanel(
    panel: CustomPanelAPI,
    capabilities: ShipEditorCapabilities,
) {
    val modifiersText = capabilities.modifierText() ?: return
    CampaignModifiersPanel.buildCampaignModifiersPanel(panel, modifiersText)
}

fun estimateCampaignOptionModifiersHeight(capabilities: ShipEditorCapabilities): Float {
    return if (capabilities.modifierText() == null) 0f else CampaignModifiersPanel.HEIGHT
}
