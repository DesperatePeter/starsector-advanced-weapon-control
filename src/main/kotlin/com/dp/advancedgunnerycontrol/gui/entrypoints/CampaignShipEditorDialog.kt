package com.dp.advancedgunnerycontrol.gui.entrypoints

import com.dp.advancedgunnerycontrol.gui.controls.input.CampaignKeyboardShortcuts
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.options.controllers.CampaignOptionsPanelController
import com.dp.advancedgunnerycontrol.gui.panels.content.replaceCampaignContentWithErrorFallback
import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.shipview.ShipView
import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.actions.GoToSuggestedTagsAction
import com.dp.advancedgunnerycontrol.gui.actions.generateShipActions
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import org.lwjgl.input.Keyboard

/**
 * Campaign interaction-dialog wrapper for the ship editor.
 * Used when ACG is opened from the campaign-style AGC GUI flow.
 */
class CampaignShipEditorDialogDelegate(
    private val panelPlugin: CampaignShipEditorPanelPlugin,
) : CustomVisualDialogDelegate {
    override fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        panelPlugin.init(panel, callbacks)
    }

    override fun getCustomPanelPlugin(): CustomUIPanelPlugin = panelPlugin

    override fun getNoiseAlpha(): Float = 0f

    override fun advance(amount: Float) {}

    override fun reportDismissed(option: Int) {}
}

/**
 * Campaign ship-editor host component.
 * Owns the campaign-side root panel, option buttons, modal confirmation routing,
 * and the shared ShipView instance.
 */
class CampaignShipEditorPanelPlugin(
    private val attributes: GUIAttributes,
    private val onBackToPicker: () -> Unit,
) : BaseCustomUIPanelPlugin() {
    private val log = Global.getLogger(CampaignShipEditorPanelPlugin::class.java)
    private var panel: CustomPanelAPI? = null
    private var callbacks: CustomVisualDialogDelegate.DialogCallbacks? = null
    private var buttonListenerPanel: CustomPanelAPI? = null

    private var contentPanel: CustomPanelAPI? = null
    private var shipView: ShipView? = null
    private var shipViewSessionState = ShipViewSessionState()
    private val optionsPanelController = CampaignOptionsPanelController(
        buttonListenerProvider = { buttonListenerPanel },
        executeAction = ::executeAction,
        requestRebuild = ::rebuild,
        requestConfirmationRefresh = ::openOptionConfirmationModal,
        capabilities = ShipEditorCapabilities.CAMPAIGN,
        afterResetAction = { shipView?.markAllVisibleGroupsCleanToCurrentTags() },
        extraRowsProvider = { shipView?.allGroupsPresetOptionRows().orEmpty() },
    )

    fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        this.panel = panel
        this.callbacks = callbacks
        this.buttonListenerPanel = Global.getSettings().createCustom(0f, 0f, this)
        log.info("[AGC_CAMPAIGN_UI] init root=${panel.position.width}x${panel.position.height}")
        rebuild()
    }

    override fun render(alphaMult: Float) {
    }

    override fun advance(amount: Float) {
        optionsPanelController.advance()
        if (optionsPanelController.consumeModifierChange()) {
            rebuild()
            return
        }

        if (shipView?.shouldRegenerate() == true) {
            rebuild()
        }
    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        shipView?.processInput(events)
        optionsPanelController.processInput(events)
        CampaignKeyboardShortcuts.processCampaignKeyboardShortcutInput(
            events = events,
            closeKeys = setOf(Keyboard.KEY_ESCAPE, Settings.guiHotkey()),
            onClose = ::dismissToPicker,
            onShortcut = { key ->
                optionsPanelController.handleShortcut(key)
            },
        )
        CampaignButtonSuppression.consumeUnhandledAgcEditorInput(events)
    }

    override fun buttonPressed(buttonId: Any?) {
        optionsPanelController.handleButtonId(buttonId)
    }

    private fun rebuild() {
        val root = panel ?: return
        val ship = attributes.ship ?: return
        val startNs = System.nanoTime()
        try {
            optionsPanelController.updateActions(generateShipActions(attributes, ShipEditorCapabilities.CAMPAIGN))
            val content = rebuildShipEditorShipViewContent(
                root = root,
                content = contentPanel,
                shipView = shipView,
                ship = ship,
                tagView = attributes.tagView,
                sessionState = shipViewSessionState,
                onSessionStateUpdate = { state -> shipViewSessionState = state },
                customListState = attributes,
                externalConfirmationModalProvider = optionsPanelController::confirmationRequest,
                suppressExternalOptionHover = optionsPanelController::suppressButtonHover,
                restoreExternalOptionHover = optionsPanelController::restoreButtonHover,
                onCleared = ::clearContentRefs,
                buildOptionsPanel = optionsPanelController::buildPanel,
                optionsPreferredHeightProvider = optionsPanelController::estimateHeight,
                stableOptionsPreferredHeightProvider = optionsPanelController::estimateStableHeight,
                buildModifiersPanel = optionsPanelController::buildModifiersPanel,
                modifiersPreferredHeightProvider = optionsPanelController::estimateModifiersHeight,
                beforeBuild = { view ->
                    shipView = view
                },
            )
            contentPanel = content.panel
            shipView = content.view
            shipViewSessionState = content.sessionState
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (elapsedMs >= CampaignGuiStyle.GUI_PERF_LOG_THRESHOLD_MS) {
                log.info("[AGC_PERF] Campaign ship editor rebuild took ${elapsedMs}ms")
            }
        } catch (ex: Throwable) {
            log.error("[AGC_CAMPAIGN_UI] rebuild failed", ex)
            buildFallback(root, ex)
        }
    }

    private fun buildFallback(root: CustomPanelAPI, ex: Throwable) {
        contentPanel = replaceCampaignContentWithErrorFallback(
            root = root,
            content = contentPanel,
            title = "AGC Campaign UI Error",
            message = "The campaign editor failed to build. Press [Esc] or the AGC GUI hotkey to exit this screen.",
            reason = "Reason: ${ex.javaClass.simpleName}: ${ex.message ?: "no message"}",
            highlightToken = "[Esc]",
            onCleared = ::clearContentRefs,
        )
    }

    private fun clearContentRefs() {
        contentPanel = null
        shipView = null
    }

    private fun dismissToPicker() {
        callbacks?.dismissDialog()
        onBackToPicker()
    }

    private fun executeAction(action: GUIAction) {
        action.execute()
        when {
            action is GoToSuggestedTagsAction -> {
                callbacks?.dismissDialog()
            }
            else -> {
                rebuild()
            }
        }
    }

    private fun openOptionConfirmationModal() {
        shipView?.openExternalConfirmationModalOrDirty() ?: rebuild()
    }
}
