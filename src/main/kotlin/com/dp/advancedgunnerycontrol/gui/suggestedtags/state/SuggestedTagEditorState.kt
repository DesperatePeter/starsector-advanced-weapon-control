package com.dp.advancedgunnerycontrol.gui.suggestedtags.state

import com.dp.advancedgunnerycontrol.gui.controls.confirmations.PendingConfirmationState
import com.dp.advancedgunnerycontrol.gui.controls.input.CampaignKeyboardShortcuts

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagDangerousAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.buildSuggestedTagActionRows
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.pendingSuggestedDangerousActionRequest
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.processSuggestedConfirmationModalInput
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.SuggestedTagGuiView
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

class SuggestedTagEditorState {
    companion object {
        private val SUGGESTED_TAG_LIST_MODES = SuggestedTagEditorPreferences.listModes()
    }

    private val weaponListView = WeaponListView(5)
    private var suggestedTagListMode: WeaponTagListMode = SuggestedTagEditorPreferences.activeListMode()

    var sessionState = SuggestedTagSessionState()
        private set

    private val optionsController = SuggestedTagOptionsController(
        weaponListView = weaponListView,
        sessionStateProvider = { sessionState },
        updateSessionState = { state -> sessionState = state },
    )
    private var currentActions: List<SuggestedTagUiAction> = emptyList()
    private val pendingDangerousAction = PendingConfirmationState<SuggestedTagDangerousAction>()

    fun ensureSuggestedTagsInitialized() {
        if (Settings.customSuggestedTags.isEmpty()) {
            Settings.customSuggestedTags = Settings.defaultSuggestedTags
        }
    }

    fun captureFrom(view: SuggestedTagGuiView?) {
        sessionState = sessionState.captureFrom(view)
    }

    fun refreshActions() {
        currentActions = buildSuggestedTagActionRows(
            weaponListView = weaponListView,
            suggestedTagListMode = suggestedTagListMode,
            suggestedTagListModeCount = SUGGESTED_TAG_LIST_MODES.size,
            suggestedTagListModeIndex = SUGGESTED_TAG_LIST_MODES.indexOf(suggestedTagListMode).coerceAtLeast(0),
            cycleSuggestedTagListMode = ::cycleSuggestedTagListMode,
            clearFilters = optionsController::clearFilters,
            armDangerousAction = pendingDangerousAction::arm,
        )
    }

    fun visibleSuggestedTagList(): List<String> {
        return when (suggestedTagListMode) {
            WeaponTagListMode.CUSTOM_GLOBAL -> CustomWeaponTagListStore.getGlobalSupportedCustomTags()
            WeaponTagListMode.NOVICE,
            WeaponTagListMode.CLASSIC,
            WeaponTagListMode.COMPLETE -> Settings.getWeaponTagListForMode(suggestedTagListMode)
            WeaponTagListMode.CUSTOM -> Settings.getWeaponTagListForMode(WeaponTagListMode.CLASSIC)
        }
    }

    private fun cycleSuggestedTagListMode(delta: Int) {
        val currentIndex = SUGGESTED_TAG_LIST_MODES.indexOf(suggestedTagListMode).coerceAtLeast(0)
        val nextIndex = (currentIndex + delta + SUGGESTED_TAG_LIST_MODES.size) % SUGGESTED_TAG_LIST_MODES.size
        suggestedTagListMode = SUGGESTED_TAG_LIST_MODES[nextIndex]
        SuggestedTagEditorPreferences.saveListMode(suggestedTagListMode)
    }

    fun shouldRegenerate(view: SuggestedTagGuiView?): Boolean {
        return view?.shouldRegenerate() == true || weaponListView.hasChanged()
    }

    fun rebuildContent(
        root: CustomPanelAPI,
        content: CustomPanelAPI?,
        view: SuggestedTagGuiView?,
        onCleared: () -> Unit,
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
        renderConfirmationModal: (CustomPanelAPI) -> Unit,
    ): SuggestedTagEditorContent {
        ensureSuggestedTagsInitialized()
        captureFrom(view)
        refreshActions()
        return SuggestedTagContentLifecycle.rebuildContent(
            weaponListView = weaponListView,
            sessionState = sessionState,
            visibleTagList = visibleSuggestedTagList(),
            root = root,
            content = content,
            onCleared = onCleared,
            buildOptionsPanel = buildOptionsPanel,
            renderConfirmationModal = renderConfirmationModal,
        )
    }

    fun refreshContent(
        view: SuggestedTagGuiView?,
        buildOptionsPanel: (CustomPanelAPI) -> Unit,
        rebuild: () -> Unit,
    ) {
        ensureSuggestedTagsInitialized()
        captureFrom(view)
        refreshActions()
        if (!SuggestedTagContentLifecycle.refreshContent(
                weaponListView = weaponListView,
                view = view,
                visibleTagList = visibleSuggestedTagList(),
                buildOptionsPanel = buildOptionsPanel,
            )
        ) {
            rebuild()
            return
        }
    }

    fun renderOptionsPanel(
        panel: CustomPanelAPI,
        bindButton: (SuggestedTagUiAction, ButtonAPI, Boolean) -> Unit,
    ) {
        optionsController.renderOptionsPanel(
            panel = panel,
            currentActions = currentActions,
            bindButton = bindButton,
        )
    }

    fun processInput(
        events: MutableList<InputEventAPI>,
        view: SuggestedTagGuiView?,
        panelPos: PositionAPI?,
        closeKeys: Set<Int>,
        onClose: () -> Unit,
        rebuild: () -> Unit,
        refreshContent: () -> Unit = rebuild,
        requestConfirmationRefresh: () -> Unit = rebuild,
        closeConfirmationTargeted: () -> Unit = rebuild,
    ) {
        if (
            processSuggestedConfirmationModalInput(
                events,
                ::pendingConfirmationRequest,
                rebuild,
                afterCancel = closeConfirmationTargeted,
            )
        ) return
        if (optionsController.processScrollInput(
                events = events,
                panelPos = panelPos,
                onScrolled = refreshContent,
            )
        ) return
        view?.processInput(events)
        CampaignKeyboardShortcuts.processCampaignKeyboardShortcutInput(
            events = events,
            closeKeys = closeKeys,
            onClose = onClose,
            onShortcut = { key ->
                val action = currentActions.firstOrNull { key in it.shortcuts }
                    ?: return@processCampaignKeyboardShortcutInput false
                executeAction(
                    action,
                    rebuild,
                    refreshContent = refreshContent,
                    requestConfirmationRefresh = requestConfirmationRefresh,
                )
                true
            },
        )
    }

    fun executeAction(
        action: SuggestedTagUiAction,
        rebuild: () -> Unit,
        refreshContent: () -> Unit = rebuild,
        requestConfirmationRefresh: () -> Unit = rebuild,
    ) {
        val hadPendingConfirmation = pendingConfirmationRequest() != null
        action.callback()
        val hasPendingConfirmation = pendingConfirmationRequest() != null
        when {
            action.rebuildAfter -> refreshContent()
            !hadPendingConfirmation && hasPendingConfirmation -> requestConfirmationRefresh()
        }
    }

    fun pendingConfirmationRequest() = pendingSuggestedDangerousActionRequest(pendingDangerousAction)

    fun clearPendingConfirmation() {
        pendingDangerousAction.clear()
    }
}
