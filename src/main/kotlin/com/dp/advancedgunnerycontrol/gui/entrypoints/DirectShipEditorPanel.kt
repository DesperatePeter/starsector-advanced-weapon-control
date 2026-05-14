package com.dp.advancedgunnerycontrol.gui.entrypoints

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonCallbackPoller
import com.dp.advancedgunnerycontrol.gui.controls.input.CampaignKeyboardShortcuts
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppressionSnapshot
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.options.controllers.DirectShipEditorOptionsController
import com.dp.advancedgunnerycontrol.gui.modals.*
import com.dp.advancedgunnerycontrol.gui.panels.content.replaceCampaignContentWithErrorFallback
import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.shipview.ShipView
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.SuggestedConfirmationModalIds
import com.dp.advancedgunnerycontrol.gui.suggestedtags.state.SuggestedTagEditorState
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.modals.renderSuggestedConfirmationModal as renderSuggestedConfirmationModalShared
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.SuggestedTagGuiView
import com.dp.advancedgunnerycontrol.reflection.invokeMethodByName
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.state.AppDriver
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11

/**
 * Current-screen ship-editor overlay component.
 * Used by refit and direct-opening flows to host the shared ShipView with a
 * live ShipAPI plus persistent fleet-member storage.
 */
class DirectShipEditorPanel(
    private val parent: UIPanelAPI,
    private val ship: ShipAPI,
    private val closeHotkey: Int,
    private val onClosed: () -> Unit,
) : BaseCustomUIPanelPlugin() {
    companion object {
        private val DIRECT_SUGGESTED_CONFIRMATION_IDS = SuggestedConfirmationModalIds(
            confirm = "direct_suggested_confirmation_confirm",
            cancel = "direct_suggested_confirmation_cancel",
            backdrop = "direct_suggested_confirmation_backdrop",
        )

        fun openOnCurrentScreen(ship: ShipAPI, closeHotkey: Int, onClosed: () -> Unit): DirectShipEditorPanel? {
            val state = AppDriver.getInstance()?.currentState ?: return null
            val screenPanel = invokeMethodByName(
                "getScreenPanel",
                state,
                narrativeContext = "Direct AGC ship editor, get screen panel"
            ) as? UIPanelAPI ?: return null
            return open(screenPanel, ship, closeHotkey, onClosed)
        }

        fun open(parent: UIPanelAPI, ship: ShipAPI, closeHotkey: Int, onClosed: () -> Unit): DirectShipEditorPanel? {
            val plugin = DirectShipEditorPanel(parent, ship, closeHotkey, onClosed)
            val width = parent.position.width
            val height = parent.position.height
            val root = Global.getSettings().createCustom(width, height, plugin) ?: return null
            val rootPosition = parent.addComponent(root) ?: return null
            plugin.rootPanel = root
            rootPosition.inTL(0f, 0f)
            plugin.rebuild()
            return plugin
        }
    }

    private val log = Global.getLogger(DirectShipEditorPanel::class.java)
    private val tagView = com.dp.advancedgunnerycontrol.gui.session.TagListView()
    private val optionsController = DirectShipEditorOptionsController(
        activeMemberProvider = ::activeMember,
        activeRuntimeShipProvider = ::activeRuntimeShip,
        selectAdjacentShip = ::selectAdjacentShip,
        tagView = tagView,
        requestRebuild = ::requestRebuild,
        requestConfirmationRefresh = ::openOptionConfirmationModal,
        openSuggestedTags = ::openSuggestedTags,
        capabilities = ShipEditorCapabilities.DIRECT,
        afterResetAction = { shipView?.markAllVisibleGroupsCleanToCurrentTags() },
        extraRowsProvider = { shipView?.allGroupsPresetOptionRows().orEmpty() },
    )
    private enum class DirectEditorPage {
        SHIP_EDITOR,
        SUGGESTED_TAGS,
    }

    private var rootPanel: CustomPanelAPI? = null
    private var contentPanel: CustomPanelAPI? = null
    private var shipView: ShipView? = null
    private var suggestedView: SuggestedTagGuiView? = null
    private var position: PositionAPI? = null
    private var closed = false
    private var rebuildRequested = false
    private var currentPage = DirectEditorPage.SHIP_EDITOR
    private var shipViewSessionState = ShipViewSessionState()
    private val suggestedEditorState = SuggestedTagEditorState()
    private val suggestedActionButtons = mutableListOf<ButtonBase<*>>()
    private val suggestedActionButtonPoller = CampaignButtonCallbackPoller(suggestedActionButtons)
    private var suggestedConfirmationRootPanel: CustomPanelAPI? = null
    private var suggestedConfirmationModal: RenderedCampaignConfirmationModal? = null
    private var suggestedConfirmationButtonStartIndex: Int = -1
    private var suggestedConfirmationButtonEndIndex: Int = -1
    private var suggestedNonModalSuppressionSnapshot: CampaignButtonSuppressionSnapshot? = null
    private var activeFleetMember: FleetMemberAPI? = ship.fleetMember

    override fun positionChanged(position: PositionAPI?) {
        this.position = position
    }

    override fun renderBelow(alphaMult: Float) {
        val pos = position ?: return
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glPushMatrix()
        try {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColor4f(0f, 0f, 0f, 1f * alphaMult)
            GL11.glRectf(
                0f,
                0f,
                Global.getSettings().screenWidth,
                Global.getSettings().screenHeight
            )
            GL11.glRectf(pos.x, pos.y, pos.x + pos.width, pos.y + pos.height)
        } finally {
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    override fun advance(amount: Float) {
        when (currentPage) {
            DirectEditorPage.SHIP_EDITOR -> {
                optionsController.advance()
                if (optionsController.consumeModifierChange()) {
                    requestRebuild()
                }
                if (shipView?.shouldRegenerate() == true) {
                    requestRebuild()
                }
            }
            DirectEditorPage.SUGGESTED_TAGS -> {
                suggestedActionButtonPoller.processIfRequested()
                suggestedView?.advance(amount)
                if (suggestedEditorState.shouldRegenerate(suggestedView)) {
                    requestRebuild()
                }
            }
        }
        if (rebuildRequested) {
            rebuildRequested = false
            rebuild()
        }
    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        when (currentPage) {
            DirectEditorPage.SHIP_EDITOR -> processShipEditorInput(events)
            DirectEditorPage.SUGGESTED_TAGS -> processSuggestedTagsInput(events)
        }
        // Starsector delivers mouse movement/clicks to the hidden refit/combat
        // screen underneath this direct editor unless AGC consumes leftovers.
        CampaignButtonSuppression.consumeUnhandledAgcEditorInput(events)
    }

    override fun buttonPressed(buttonId: Any?) {}

    fun close() {
        if (closed) return
        closed = true
        try {
            rootPanel?.let { panel ->
                runCatching { parent.removeComponent(panel) }
                    .onFailure { ex ->
                        log.warn("[AGC_DIRECT_EDITOR] Failed to remove root panel during close", ex)
                    }
            }
        } finally {
            rootPanel = null
            contentPanel = null
            shipView = null
            suggestedView = null
            runCatching { onClosed() }
                .onFailure { ex ->
                    log.warn("[AGC_DIRECT_EDITOR] Close cleanup callback failed", ex)
                }
        }
    }

    private fun rebuild() {
        when (currentPage) {
            DirectEditorPage.SHIP_EDITOR -> rebuildShipEditor()
            DirectEditorPage.SUGGESTED_TAGS -> rebuildSuggestedTags()
        }
    }

    private fun rebuildShipEditor() {
        val root = rootPanel ?: return
        val member = activeMember() ?: return
        val runtimeShip = activeRuntimeShip()
        val startNs = System.nanoTime()
        try {
            AGCGUI.storageIndex = Values.storageIndex
            optionsController.rebuildRows()
            val content = rebuildShipEditorShipViewContent(
                root = root,
                content = contentPanel,
                shipView = shipView,
                ship = member,
                tagView = tagView,
                sessionState = shipViewSessionState,
                onSessionStateUpdate = { state -> shipViewSessionState = state },
                customListState = optionsController.customListStateAttributes(),
                runtimeShip = runtimeShip,
                externalConfirmationModalProvider = optionsController::confirmationRequest,
                suppressExternalOptionHover = optionsController::suppressButtonHover,
                restoreExternalOptionHover = optionsController::restoreButtonHover,
                onCleared = ::clearContentRefs,
                buildOptionsPanel = optionsController::buildPanel,
                optionsPreferredHeightProvider = optionsController::estimateHeight,
                stableOptionsPreferredHeightProvider = optionsController::estimateStableHeight,
                buildModifiersPanel = optionsController::buildModifiersPanel,
                modifiersPreferredHeightProvider = optionsController::estimateModifiersHeight,
                beforeBuild = { view ->
                    shipView = view
                },
            )
            contentPanel = content.panel
            shipView = content.view
            shipViewSessionState = content.sessionState
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (elapsedMs >= CampaignGuiStyle.GUI_PERF_LOG_THRESHOLD_MS) {
                log.info("[AGC_PERF] Direct ship editor rebuild took ${elapsedMs}ms")
            }
        } catch (ex: Throwable) {
            log.error("[AGC_DIRECT_SHIP_EDITOR] rebuild failed", ex)
            contentPanel = replaceCampaignContentWithErrorFallback(
                root = root,
                content = contentPanel,
                title = "AGC Direct Ship Editor Error",
                message = "The direct ship editor failed to build. Press [Esc] or the AGC GUI hotkey to close this screen.",
                reason = "Reason: ${ex.javaClass.simpleName}: ${ex.message ?: "no message"}",
                highlightToken = "[Esc]",
                onCleared = ::clearContentRefs,
            )
        }
    }

    private fun rebuildSuggestedTags() {
        val root = rootPanel ?: return
        val startNs = System.nanoTime()
        try {
            val content = suggestedEditorState.rebuildContent(
                root = root,
                content = contentPanel,
                view = suggestedView,
                onCleared = ::clearContentRefs,
                buildOptionsPanel = ::buildSuggestedOptionsPanel,
                renderConfirmationModal = ::renderSuggestedConfirmationModal,
            )
            contentPanel = content.panel
            suggestedView = content.view
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (elapsedMs >= CampaignGuiStyle.GUI_PERF_LOG_THRESHOLD_MS) {
                log.info("[AGC_PERF] Direct suggested-tags rebuild took ${elapsedMs}ms")
            }
        } catch (ex: Throwable) {
            log.error("[AGC_DIRECT_SUGGESTED_TAGS] rebuild failed", ex)
            contentPanel = replaceCampaignContentWithErrorFallback(
                root = root,
                content = contentPanel,
                title = "AGC Direct Suggested Tags Error",
                message = "The suggested-tags editor failed to build. Press [Esc] to return to weapon groups.",
                reason = "Reason: ${ex.javaClass.simpleName}: ${ex.message ?: "no message"}",
                highlightToken = "[Esc]",
                onCleared = ::clearContentRefs,
            )
        }
    }

    private fun clearContentRefs() {
        clearSuggestedConfirmationModalRefs()
        suggestedNonModalSuppressionSnapshot = null
        contentPanel = null
        shipView = null
        suggestedView = null
    }

    private fun processShipEditorInput(events: MutableList<InputEventAPI>) {
        shipView?.processInput(events)
        optionsController.processInput(events)
        CampaignKeyboardShortcuts.processCampaignKeyboardShortcutInput(
            events = events,
            closeKeys = setOf(Keyboard.KEY_ESCAPE, closeHotkey),
            onClose = ::close,
            onShortcut = optionsController::handleShortcut,
        )
    }

    private fun processSuggestedTagsInput(events: MutableList<InputEventAPI>) {
        suggestedActionButtonPoller.requestPollFromInput(events)
        if (CampaignButtonControls.processRightClickInput(suggestedActionButtons, events)) return
        suggestedEditorState.processInput(
            events = events,
            view = suggestedView,
            panelPos = contentPanel?.position,
            closeKeys = setOf(Keyboard.KEY_ESCAPE, closeHotkey),
            onClose = ::returnToShipEditor,
            rebuild = ::requestRebuild,
            refreshContent = ::refreshSuggestedContentOrRebuild,
            requestConfirmationRefresh = ::openSuggestedConfirmationModalOrRebuild,
            closeConfirmationTargeted = ::closeSuggestedConfirmationModalTargeted,
        )
    }

    private fun openSuggestedTags() {
        suggestedEditorState.clearPendingConfirmation()
        shipViewSessionState = shipViewSessionState.copy(
            tagExpandedCategoryTitles = emptyMap(),
            collapsedPanelIds = emptySet()
        )
        currentPage = DirectEditorPage.SUGGESTED_TAGS
        requestRebuild()
    }

    private fun returnToShipEditor() {
        suggestedEditorState.clearPendingConfirmation()
        shipViewSessionState = shipViewSessionState.copy(
            tagExpandedCategoryTitles = emptyMap(),
            collapsedPanelIds = emptySet()
        )
        currentPage = DirectEditorPage.SHIP_EDITOR
        requestRebuild()
    }

    private fun buildSuggestedOptionsPanel(panel: CustomPanelAPI) {
        suggestedActionButtons.clear()
        suggestedEditorState.renderOptionsPanel(
            panel = panel,
            bindButton = { action, button, stateful ->
                bindSuggestedActionButton(action, button, stateful = stateful)
            },
        )
    }

    private fun refreshSuggestedContentOrRebuild() {
        suggestedEditorState.refreshContent(
            view = suggestedView,
            buildOptionsPanel = ::buildSuggestedOptionsPanel,
            rebuild = ::requestRebuild,
        )
    }

    private fun bindSuggestedActionButton(
        action: SuggestedTagUiAction,
        button: ButtonAPI,
        stateful: Boolean = false,
    ) {
        val checkedVisualState = action.usesCheckedVisualState(stateful)
        val control = CampaignButtonControls.addControl(suggestedActionButtons,
            button = button,
            active = action.active && checkedVisualState,
            stateful = checkedVisualState,
        ) {
            executeSuggestedAction(action)
        }
        action.onRightClick?.let { rightClick ->
            control.onRightClick {
                rightClick()
                refreshSuggestedContentOrRebuild()
            }
        }
    }

    private fun executeSuggestedAction(action: SuggestedTagUiAction) {
        suggestedEditorState.executeAction(
            action,
            rebuild = ::requestRebuild,
            refreshContent = ::refreshSuggestedContentOrRebuild,
            requestConfirmationRefresh = ::openSuggestedConfirmationModalOrRebuild,
        )
    }

    private fun pendingSuggestedConfirmationRequest() = suggestedEditorState.pendingConfirmationRequest()

    private fun renderSuggestedConfirmationModal(panel: CustomPanelAPI) {
        val startIndex = suggestedActionButtons.size
        val modal = renderSuggestedConfirmationModalShared(
            root = panel,
            request = pendingSuggestedConfirmationRequest(),
            ids = DIRECT_SUGGESTED_CONFIRMATION_IDS,
            suppressNonModalButtonHover = ::suppressSuggestedNonModalButtonHover,
        ) { modal, request ->
            CampaignButtonControls.addRenderedConfirmationModalButtons(suggestedActionButtons,
                modal = modal,
                onConfirm = {
                    request.onConfirm()
                    requestRebuild()
                },
                onCancel = {
                    request.onCancel()
                    closeSuggestedConfirmationModalTargeted()
                },
            )
        } ?: return
        suggestedConfirmationRootPanel = panel
        suggestedConfirmationModal = modal
        suggestedConfirmationButtonStartIndex = startIndex
        suggestedConfirmationButtonEndIndex = suggestedActionButtons.size
    }

    private fun suppressSuggestedNonModalButtonHover() {
        if (suggestedNonModalSuppressionSnapshot != null) return
        suggestedActionButtons.forEach { button -> button.syncVisualCheckedToActive() }
        suggestedNonModalSuppressionSnapshot = CampaignButtonSuppression.suppressRegisteredCampaignButtonHoverSnapshot()
    }

    private fun openSuggestedConfirmationModalOrRebuild() {
        val panel = contentPanel ?: run {
            requestRebuild()
            return
        }
        if (suggestedConfirmationModal != null) {
            closeSuggestedConfirmationModalTargeted()
        }
        renderSuggestedConfirmationModal(panel)
    }

    private fun closeSuggestedConfirmationModalTargeted() {
        val panel = suggestedConfirmationRootPanel
        val modal = suggestedConfirmationModal
        if (panel == null || modal == null) {
            requestRebuild()
            return
        }
        try {
            runCatching { panel.removeComponent(modal.backdrop) }
                .onFailure { ex ->
                    log.warn("[AGC_DIRECT_EDITOR] Failed to remove suggested confirmation backdrop", ex)
                }
            runCatching { panel.removeComponent(modal.dialog) }
                .onFailure { ex ->
                    log.warn("[AGC_DIRECT_EDITOR] Failed to remove suggested confirmation dialog", ex)
                }
            CampaignButtonControls.clearRenderedConfirmationModalButtons(suggestedActionButtons,
                suggestedConfirmationButtonStartIndex,
                suggestedConfirmationButtonEndIndex,
            )
        } finally {
            clearSuggestedConfirmationModalRefs()
            restoreSuggestedSuppressedNonModalButtonHover()
        }
    }

    private fun clearSuggestedConfirmationModalRefs() {
        suggestedConfirmationRootPanel = null
        suggestedConfirmationModal = null
        suggestedConfirmationButtonStartIndex = -1
        suggestedConfirmationButtonEndIndex = -1
    }

    private fun restoreSuggestedSuppressedNonModalButtonHover() {
        suggestedNonModalSuppressionSnapshot?.restore()
        suggestedNonModalSuppressionSnapshot = null
    }

    private fun requestRebuild() {
        rebuildRequested = true
    }

    private fun openOptionConfirmationModal() {
        shipView?.openExternalConfirmationModalOrDirty() ?: requestRebuild()
    }

    private fun activeMember(): FleetMemberAPI? {
        return activeFleetMember ?: ship.fleetMember
    }

    private fun activeRuntimeShip(): ShipAPI? {
        val member = activeMember() ?: return null
        if (shipMatchesMember(ship, member)) return ship
        return Global.getCombatEngine()?.ships?.firstOrNull { candidate ->
            candidate != null && shipMatchesMember(candidate, member)
        }
    }

    private fun selectAdjacentShip(previous: Boolean) {
        val members = editableFleetMembers()
        if (members.isEmpty()) return
        val current = activeMember()
        val currentIndex = members.indexOfFirst { it.id == current?.id }.takeIf { it >= 0 } ?: 0
        val nextIndex = if (previous) {
            if (currentIndex == 0) members.lastIndex else currentIndex - 1
        } else {
            if (currentIndex >= members.lastIndex) 0 else currentIndex + 1
        }
        activeFleetMember = members[nextIndex]
        shipViewSessionState = shipViewSessionState.copy(
            tagScrollOffsets = emptyMap(),
            optionsScrollOffset = 0,
            presetControlStates = emptyMap()
        )
        tagView.reset()
    }

    private fun editableFleetMembers(): List<FleetMemberAPI> {
        return Global.getSector()?.playerFleet?.membersWithFightersCopy
            ?.filterNot { member -> member.isFighterWing }
            ?: activeMember()?.let(::listOf)
            ?: emptyList()
    }

    private fun shipMatchesMember(candidate: ShipAPI, member: FleetMemberAPI): Boolean {
        return candidate.fleetMember?.id == member.id || candidate.fleetMemberId == member.id
    }

}
