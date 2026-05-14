package com.dp.advancedgunnerycontrol.gui.suggestedtags

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonCallbackPoller
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppressionSnapshot
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.modals.RenderedCampaignConfirmationModal
import com.dp.advancedgunnerycontrol.gui.panels.content.replaceCampaignContentWithErrorFallback
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.SuggestedConfirmationModalIds
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.renderSuggestedConfirmationModal
import com.dp.advancedgunnerycontrol.gui.suggestedtags.state.SuggestedTagEditorState
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.SuggestedTagGuiView
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import org.lwjgl.input.Keyboard

/**
 * Campaign suggested-tags host component.
 * Owns the dialog root, option buttons, confirmation popup, and shared
 * SuggestedTagEditorState content rebuilds.
 */
internal class SuggestedTagPanelPlugin(
    private val editorState: SuggestedTagEditorState,
    private val onClose: (CustomVisualDialogDelegate.DialogCallbacks?) -> Unit,
) : BaseCustomUIPanelPlugin() {
    private val log = Global.getLogger(SuggestedTagPanelPlugin::class.java)
    private var panel: CustomPanelAPI? = null
    private var callbacks: CustomVisualDialogDelegate.DialogCallbacks? = null
    private var contentPanel: CustomPanelAPI? = null
    private var view: SuggestedTagGuiView? = null
    private val actionButtons = mutableListOf<ButtonBase<*>>()
    private val actionButtonPoller = CampaignButtonCallbackPoller(actionButtons)
    private var confirmationRootPanel: CustomPanelAPI? = null
    private var confirmationModal: RenderedCampaignConfirmationModal? = null
    private var confirmationButtonStartIndex: Int = -1
    private var confirmationButtonEndIndex: Int = -1
    private var nonModalSuppressionSnapshot: CampaignButtonSuppressionSnapshot? = null

    fun init(panel: CustomPanelAPI, callbacks: CustomVisualDialogDelegate.DialogCallbacks) {
        this.panel = panel
        this.callbacks = callbacks
        rebuild()
    }

    override fun advance(amount: Float) {
        actionButtonPoller.processIfRequested()
        if (editorState.shouldRegenerate(view)) {
            rebuild()
        }
    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        actionButtonPoller.requestPollFromInput(events)
        if (CampaignButtonControls.processRightClickInput(actionButtons, events)) return
        editorState.processInput(
            events = events,
            view = view,
            panelPos = contentPanel?.position,
            closeKeys = setOf(Keyboard.KEY_ESCAPE, Settings.guiHotkey()),
            onClose = { onClose(callbacks) },
            rebuild = ::rebuild,
            refreshContent = ::refreshContentOrRebuild,
            requestConfirmationRefresh = ::openConfirmationModalOrRebuild,
            closeConfirmationTargeted = ::closeConfirmationModalTargeted,
        )
    }

    override fun buttonPressed(buttonId: Any?) {}

    private fun executeAction(action: SuggestedTagUiAction) {
        editorState.executeAction(
            action,
            rebuild = ::rebuild,
            refreshContent = ::refreshContentOrRebuild,
            requestConfirmationRefresh = ::openConfirmationModalOrRebuild,
        )
    }

    private fun rebuild() {
        val root = panel ?: return
        val startNs = System.nanoTime()
        try {
            val content = editorState.rebuildContent(
                root = root,
                content = contentPanel,
                view = view,
                onCleared = ::clearContentRefs,
                buildOptionsPanel = ::buildOptionsPanel,
                renderConfirmationModal = ::renderConfirmationModal,
            )
            contentPanel = content.panel
            view = content.view
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (elapsedMs >= CampaignGuiStyle.GUI_PERF_LOG_THRESHOLD_MS) {
                log.info("[AGC_PERF] Campaign suggested-tags rebuild took ${elapsedMs}ms")
            }
        } catch (ex: Throwable) {
            log.error("[AGC_SUGGESTED_UI] rebuild failed", ex)
            buildFallback(root, ex)
        }
    }

    private fun buildFallback(root: CustomPanelAPI, ex: Throwable) {
        contentPanel = replaceCampaignContentWithErrorFallback(
            root = root,
            content = contentPanel,
            title = "AGC Suggested Tags UI Error",
            message = "The suggested-tags editor failed to build. Press [Esc] to return.",
            reason = "Reason: ${ex.javaClass.simpleName}: ${ex.message ?: "no message"}",
            highlightToken = "[Esc]",
            onCleared = ::clearContentRefs,
        )
    }

    private fun clearContentRefs() {
        clearConfirmationModalRefs()
        restoreSuppressedNonModalButtonHover()
        contentPanel = null
        view = null
    }

    private fun buildOptionsPanel(panel: CustomPanelAPI) {
        actionButtons.clear()
        editorState.renderOptionsPanel(
            panel = panel,
            bindButton = { action, button, stateful ->
                bindActionButton(action, button, stateful)
            },
        )
    }

    private fun refreshContentOrRebuild() {
        editorState.refreshContent(
            view = view,
            buildOptionsPanel = ::buildOptionsPanel,
            rebuild = ::rebuild,
        )
    }

    private fun bindActionButton(
        action: SuggestedTagUiAction,
        button: ButtonAPI,
        stateful: Boolean,
    ) {
        val checkedVisualState = action.usesCheckedVisualState(stateful)
        val control = CampaignButtonControls.addControl(actionButtons,
            button = button,
            active = action.active && checkedVisualState,
            stateful = checkedVisualState,
        ) {
            executeAction(action)
        }
        action.onRightClick?.let { rightClick ->
            control.onRightClick {
                rightClick()
                refreshContentOrRebuild()
            }
        }
    }

    private fun renderConfirmationModal(root: CustomPanelAPI) {
        val startIndex = actionButtons.size
        val modal = renderSuggestedConfirmationModal(
            root = root,
            request = editorState.pendingConfirmationRequest(),
            ids = CAMPAIGN_SUGGESTED_CONFIRMATION_IDS,
            suppressNonModalButtonHover = ::suppressNonModalButtonHover,
        ) { modal, request ->
            CampaignButtonControls.addRenderedConfirmationModalButtons(actionButtons,
                modal = modal,
                onConfirm = {
                    request.onConfirm()
                    rebuild()
                },
                onCancel = {
                    request.onCancel()
                    closeConfirmationModalTargeted()
                },
            )
        } ?: return
        confirmationRootPanel = root
        confirmationModal = modal
        confirmationButtonStartIndex = startIndex
        confirmationButtonEndIndex = actionButtons.size
    }

    private fun suppressNonModalButtonHover() {
        if (nonModalSuppressionSnapshot != null) return
        actionButtons.forEach { button -> button.syncVisualCheckedToActive() }
        nonModalSuppressionSnapshot = CampaignButtonSuppression.suppressRegisteredCampaignButtonHoverSnapshot()
    }

    private fun openConfirmationModalOrRebuild() {
        val root = contentPanel ?: run {
            rebuild()
            return
        }
        if (confirmationModal != null) {
            closeConfirmationModalTargeted()
        }
        renderConfirmationModal(root)
    }

    private fun closeConfirmationModalTargeted() {
        val root = confirmationRootPanel
        val modal = confirmationModal
        if (root != null && modal != null) {
            try {
                runCatching { root.removeComponent(modal.backdrop) }
                    .onFailure { ex ->
                        Global.getLogger(SuggestedTagPanelPlugin::class.java)
                            .warn("[AGC_SUGGESTED_TAGS] Failed to remove confirmation backdrop", ex)
                    }
                runCatching { root.removeComponent(modal.dialog) }
                    .onFailure { ex ->
                        Global.getLogger(SuggestedTagPanelPlugin::class.java)
                            .warn("[AGC_SUGGESTED_TAGS] Failed to remove confirmation dialog", ex)
                    }
                CampaignButtonControls.clearRenderedConfirmationModalButtons(actionButtons,
                    confirmationButtonStartIndex,
                    confirmationButtonEndIndex,
                )
            } finally {
                clearConfirmationModalRefs()
                restoreSuppressedNonModalButtonHover()
            }
        } else {
            rebuild()
            return
        }
    }

    private fun clearConfirmationModalRefs() {
        confirmationRootPanel = null
        confirmationModal = null
        confirmationButtonStartIndex = -1
        confirmationButtonEndIndex = -1
    }

    private fun restoreSuppressedNonModalButtonHover() {
        nonModalSuppressionSnapshot?.restore()
        nonModalSuppressionSnapshot = null
    }

    private companion object {
        val CAMPAIGN_SUGGESTED_CONFIRMATION_IDS = SuggestedConfirmationModalIds(
            confirm = "suggested_confirmation_modal_confirm",
            cancel = "suggested_confirmation_modal_cancel",
            backdrop = "suggested_confirmation_modal_backdrop",
        )
    }
}
