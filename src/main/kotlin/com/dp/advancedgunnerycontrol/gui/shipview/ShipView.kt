package com.dp.advancedgunnerycontrol.gui.shipview

import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonControls
import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.buttons.CampaignButtonCallbackPoller
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppressionSnapshot
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels
import com.dp.advancedgunnerycontrol.gui.controls.shipmodes.ShipModeButton
import com.dp.advancedgunnerycontrol.gui.controls.weapontags.TagButton

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsRenderResult
import com.dp.advancedgunnerycontrol.gui.layout.*
import com.dp.advancedgunnerycontrol.gui.presets.*
import com.dp.advancedgunnerycontrol.gui.modals.*
import com.dp.advancedgunnerycontrol.gui.panels.ship.CampaignShipPanelRenderer
import com.dp.advancedgunnerycontrol.gui.panels.weapongroups.WeaponGroupPanelCallbacks
import com.dp.advancedgunnerycontrol.gui.panels.weapongroups.WeaponGroupPanelRenderer
import com.dp.advancedgunnerycontrol.gui.panels.weapongroups.WeaponGroupTagListRenderer
import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import java.awt.Color

/**
 * Main ship-editor component used by both campaign and direct/refit AGC screens.
 * Renders the ship panel, options panel, ship modes, weapon groups, tag lists,
 * Save/Load modals, custom-list manager, edit tag/mode popups, and debug-color popup.
 *
 * Lifecycle and ownership map:
 * - session/root state: current ship, root panel, persistence context, shared buttons
 * - input routing: button polling, scroll regions, keyboard shortcuts, modal shields
 * - options panel: row builder callback, targeted row refresh, options scroll state
 * - ship modes: effective-mode cache, mode scroll state, targeted mode-panel refresh
 * - weapon groups: weapon-group panels/headings plus WeaponGroupTagListRenderer state
 * - preset modal: save/load control state, review previews, preset peek/dirty caches
 * - custom-list modal: staged tag/mode state, list scrolling, modal shell lifecycle
 * - external confirmation: host-provided warning modal rendering and cleanup
 * - persistence/cache helpers: active ShipEditorPersistenceContext, preset caches,
 *   custom-list archetype cache, and targeted refresh invalidation
 */
class ShipView(
    private val tagView: TagListView,
    private val config: ShipViewConfig = ShipViewConfig(),
    initialState: ShipViewInitialState = ShipViewInitialState(),
    private val presetBindings: ShipViewPresetBindings = ShipViewPresetBindings(),
    private val externalBindings: ShipViewExternalBindings = ShipViewExternalBindings(),
    private val customListBindings: CustomListModalBindings = CustomListModalBindings(),
) : CustomView() {
    // Session/root state and input routing.
    private val buttons: MutableList<ButtonBase<*>> = mutableListOf()
    private val log = Global.getLogger(ShipView::class.java)
    private val buttonCallbackPoller = CampaignButtonCallbackPoller(buttons)

    // Weapon-group tag lists and group heading refresh ownership.
    private val weaponGroupTagListRenderer = WeaponGroupTagListRenderer(
        initialState.tagScrollOffsets,
        initialState.tagExpandedCategoryTitles,
        buttons,
        config.runtimeShip,
        onMissingRefreshContext = { campaignScrollDirty = true },
        onGroupTagsChanged = ::handleWeaponGroupTagsChanged,
    )
    private val collapsedPanelIds = initialState.collapsedPanelIds.toMutableSet()
    private var campaignScrollDirty = false
    private var observedTagSelectionVersion = TagButton.campaignTagSelectionVersion
    private var observedShipModeSelectionVersion = ShipModeButton.campaignShipModeSelectionVersion
    private var activePersistenceContext: ShipEditorPersistenceContext? = null
    private var activeShip: FleetMemberAPI? = null
    private var rootPanel: CustomPanelAPI? = null
    private var nonModalSuppressionSnapshot: CampaignButtonSuppressionSnapshot? = null
    private val weaponGroupPanelsByIndex = mutableMapOf<Int, CustomPanelAPI>()
    private val weaponGroupHeadingPanelsByIndex = mutableMapOf<Int, CustomPanelAPI>()

    private var confirmationDialogPosition: PositionAPI? = null

    // Options panel targeted refresh and scroll state.
    private val optionsPanelController = ShipViewOptionsPanelController(
        initialScrollOffset = initialState.optionsScrollOffset,
        panelPos = { pos },
        removePanel = ::removePanelCatching,
        onBeforeRefresh = { buttonCallbackPoller.clearTargetedPoll() },
    )

    // Ship-mode effective-list cache, scroll state, and targeted refresh state.
    private val effectiveShipModeResolver = ShipViewEffectiveShipModeResolver(config.runtimeShip)
    private val shipModePanelController = ShipViewShipModePanelController(
        initialScrollOffset = initialState.shipModeScrollOffset,
        runtimeShip = config.runtimeShip,
        buttons = buttons,
        panelPos = { pos },
        configuredModes = ::effectiveShipModesForEditor,
        persistenceContext = { activePersistenceContext },
        removePanel = ::removePanelCatching,
        onBeforeRefresh = { buttonCallbackPoller.clearTargetedPoll() },
    )
    // Preset Save/Load modal, review previews, and dirty-baseline refresh ownership.
    private val presetWorkflowController = ShipViewPresetWorkflowController(
        initialStates = initialState.presetControlStates,
        runtimeShip = config.runtimeShip,
        onStateUpdate = presetBindings.onPresetControlStateUpdate,
        buttons = buttons,
        activeShip = { activeShip },
        activePersistenceContext = { activePersistenceContext },
        rootPanel = { rootPanel },
        loadoutIndex = { AGCGUI.storageIndex },
        suppressNonModalButtonHover = ::suppressNonModalButtonHover,
        restoreSuppressedNonModalButtonHover = ::restoreSuppressedNonModalButtonHover,
        setConfirmationDialogPosition = { position -> confirmationDialogPosition = position },
        confirmationDialogPosition = { confirmationDialogPosition },
        markDirty = { campaignScrollDirty = true },
        refreshWeaponGroup = weaponGroupTagListRenderer::refreshGroup,
        handleGroupTagsChanged = ::handleWeaponGroupTagsChanged,
    )
    private val customListModalController = ShipViewCustomListModalController(
        bindings = customListBindings,
        buttons = buttons,
        activeShip = { activeShip },
        setActiveShip = { ship -> activeShip = ship },
        activePersistenceContext = { activePersistenceContext },
        loadoutIndex = { AGCGUI.storageIndex },
        processRightClickEvent = ::processButtonRightClickEvent,
        suppressNonModalButtonHover = ::suppressNonModalButtonHover,
        restoreSuppressedNonModalButtonHover = ::restoreSuppressedNonModalButtonHover,
        onMutationFinished = {
            tagView.reset()
            effectiveShipModeResolver.clear()
            ShipModeButton.notifyCampaignShipModeSelectionChanged()
        },
        markDirty = { campaignScrollDirty = true },
    )

    // External confirmation modal state.
    private val externalConfirmationController = ShipViewExternalConfirmationController(
        buttons = buttons,
        suppressNonModalButtonHover = ::suppressNonModalButtonHover,
        restoreSuppressedNonModalButtonHover = ::restoreSuppressedNonModalButtonHover,
        setConfirmationDialogPosition = { position -> confirmationDialogPosition = position },
        markDirty = { campaignScrollDirty = true },
    )

    override fun advance(t: Float) {
        buttonCallbackPoller.processIfRequested()
        if (config.enableTagScroll) {
            tagView.advance()
        }
    }

    fun shouldRegenerate(): Boolean {
        return campaignScrollDirty ||
            observedTagSelectionVersion != TagButton.campaignTagSelectionVersion ||
            observedShipModeSelectionVersion != ShipModeButton.campaignShipModeSelectionVersion ||
            (config.enableTagScroll && tagView.hasChanged())
    }

    fun captureTagScrollOffsets(): Map<Int, Int> = weaponGroupTagListRenderer.captureScrollOffsets()
    fun captureTagExpandedCategoryTitles(): Map<Int, Set<String>> = weaponGroupTagListRenderer.captureExpandedCategoryTitles()
    fun captureOptionsScrollOffset(): Int = optionsPanelController.captureScrollOffset()
    fun captureShipModeScrollOffset(): Int = shipModePanelController.captureScrollOffset()
    fun capturePresetControlStates(): Map<Int, PresetControlState> = presetWorkflowController.snapshot()
    fun captureCollapsedPanelIds(): Set<String> = collapsedPanelIds.toSet()
    fun hasConfirmationModal(): Boolean = pendingConfirmationModalRequest() != null || customListModalController.hasModal()
    fun hasOverwriteWarningModal(): Boolean = hasConfirmationModal()
    fun allGroupsPresetOptionRows(): List<CampaignOptionRow> = presetWorkflowController.allGroupsOptionRows()

    fun markAllVisibleGroupsCleanToCurrentTags() {
        presetWorkflowController.markAllVisibleGroupsCleanToCurrentTags()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {
        buttonCallbackPoller.requestPollFromInput(events)
        if (customListModalController.processScrollInput(events)) return
        if (customListModalController.processModalInput(events)) return
        if (processConfirmationModalInput(events)) return
        if (optionsPanelController.processScrollInput(events)) return
        if (customListModalController.processVisibleShipModeRightClickEdit(events)) return
        if (customListModalController.processVisibleWeaponTagRightClickEdit(events)) return
        if (processButtonRightClickInput(events)) return
        if (shipModePanelController.processScrollInput(events)) return
        weaponGroupTagListRenderer.processInput(events, pos)
    }

    override fun buttonPressed(buttonId: Any?) {}

    private fun pendingPresetConfirmation(): Pair<Int, PresetControlState>? {
        return presetWorkflowController.pendingConfirmation()
    }

    private fun pendingConfirmationModalRequest(): CampaignConfirmationModalRequest? {
        return presetWorkflowController.pendingConfirmationRequest()
            ?: externalBindings.confirmationModalProvider?.invoke()
    }

    private fun processConfirmationModalInput(events: MutableList<InputEventAPI>?): Boolean {
        val pending = pendingConfirmationModalRequest() ?: return false
        return processCampaignConfirmationModalInput(
            events = events,
            onCancel = {
                pending.onCancel()
                if (pendingPresetConfirmation() == null) {
                    closeExternalConfirmationModalTargeted()
                }
            },
            onConfirm = {
                val preset = pendingPresetConfirmation()
                if (preset == null || presetWorkflowController.canConfirm(preset.first, preset.second)) {
                    pending.onConfirm()
                }
            },
            onMouseEventBeforeConsume = ::processPresetModalMouseEventBeforeConsume,
        )
    }

    private fun processPresetModalMouseEventBeforeConsume(event: InputEventAPI): Boolean {
        return processPresetScopeWheelEvent(event) || processButtonRightClickEvent(event)
    }

    private fun processPresetScopeWheelEvent(event: InputEventAPI): Boolean {
        return presetWorkflowController.processScopeWheelEvent(event)
    }

    private fun processButtonRightClickInput(events: MutableList<InputEventAPI>?): Boolean {
        return CampaignButtonControls.processRightClickInput(buttons, events)
    }

    private fun processButtonRightClickEvent(event: InputEventAPI): Boolean {
        return CampaignButtonControls.processRightClickEvent(buttons, event)
    }

    private fun isCollapsed(key: CollapsiblePanelKey): Boolean = isCollapsedPanelId(key.id)

    private fun weaponGroupWeaponsPanelId(groupIndex: Int): String = "weapon_group:$groupIndex:weapons"

    private fun isCollapsedPanelId(id: String): Boolean = id in collapsedPanelIds

    private fun toggleCollapsedPanelId(id: String) {
        if (!collapsedPanelIds.add(id)) {
            collapsedPanelIds.remove(id)
        }
        campaignScrollDirty = true
    }

    private fun addCollapsiblePanelHeading(
        panel: CustomPanelAPI,
        key: CollapsiblePanelKey,
    ): Boolean {
        return addCollapsiblePanelHeading(
            panel = panel,
            title = key.title,
            id = key.id,
        )
    }

    private fun addCollapsiblePanelHeading(
        panel: CustomPanelAPI,
        title: String,
        id: String,
        fillColor: Color = CampaignGuiStyle.COLLAPSIBLE_HEADING_COLOUR,
        statusSuffix: String = "",
    ): Boolean {
        val collapsed = isCollapsedPanelId(id)
        val shell = CampaignControlLabels.addCollapsibleCampaignPanelHeading(
            panel = panel,
            title = title,
            collapsed = collapsed,
            data = "collapse_panel:$id",
            fillColor = fillColor,
            statusSuffix = statusSuffix,
        )
        CampaignButtonControls.addControl(buttons, button = shell.button) {
            toggleCollapsedPanelId(id)
        }
        return collapsed
    }

    private fun effectiveShipModesForEditor(ship: FleetMemberAPI): List<String> =
        effectiveShipModeResolver.effectiveModesForEditor(ship, activePersistenceContext)

    private fun buildShipPanel(panel: CustomPanelAPI, ship: FleetMemberAPI) {
        if (addCollapsiblePanelHeading(panel, CollapsiblePanelKey.SHIP)) return
        CampaignShipPanelRenderer.render(panel, ship, CampaignGuiStyle.CONTAINER_HEADING_HEIGHT, renderHeading = false)
    }

    private fun removePanelCatching(parent: CustomPanelAPI, child: CustomPanelAPI, context: String) {
        runCatching { parent.removeComponent(child) }
            .onFailure { ex ->
                Global.getLogger(ShipView::class.java)
                    .warn("[AGC_GUI] Failed to remove $context panel during refresh", ex)
            }
    }

    private fun updatePresetControlState(groupIndex: Int, state: PresetControlState) {
        presetWorkflowController.update(groupIndex, state)
    }

    private fun restorePersistentPresetControlStates(ship: FleetMemberAPI) {
        presetWorkflowController.restorePersistentStates(ship)
    }

    private fun isPresetDirtyForGroup(ship: FleetMemberAPI, groupIndex: Int): Boolean {
        return presetWorkflowController.isDirtyForGroup(ship, groupIndex)
    }

    private fun isPresetDirtyForGroup(
        ship: FleetMemberAPI,
        groupIndex: Int,
        currentTags: List<String>,
    ): Boolean {
        return presetWorkflowController.isDirtyForGroup(ship, groupIndex, currentTags)
    }

    private fun handleWeaponGroupTagsChanged(groupIndex: Int, currentTags: List<String>) {
        val ship = activeShip ?: return
        val panel = weaponGroupPanelsByIndex[groupIndex] ?: return
        val isDirty = isPresetDirtyForGroup(ship, groupIndex, currentTags)
        WeaponGroupPanelRenderer.renderHeading(panel, groupIndex, isDirty, weaponGroupHeadingPanelsByIndex)
    }

    private fun buildWeaponGroupsPanel(panel: CustomPanelAPI, ship: FleetMemberAPI, contentHeight: Float, leftColumnWidth: Float) {
        WeaponGroupPanelRenderer.buildGroups(
            panel = panel,
            ship = ship,
            contentHeight = contentHeight,
            leftColumnWidth = leftColumnWidth,
            buttons = buttons,
            headingPanelsByIndex = weaponGroupHeadingPanelsByIndex,
            callbacks = weaponGroupPanelCallbacks(),
        )
    }

    private fun weaponGroupPanelCallbacks(): WeaponGroupPanelCallbacks =
        WeaponGroupPanelCallbacks(
            isCollapsed = ::isCollapsedPanelId,
            toggleCollapsed = ::toggleCollapsedPanelId,
            presetStateForGroup = presetWorkflowController::stateForGroup,
            updatePresetState = ::updatePresetControlState,
            openPresetActionModal = presetWorkflowController::openModal,
            isPresetDirty = { ship, groupIndex -> isPresetDirtyForGroup(ship, groupIndex) },
            recordGroupPanel = { groupIndex, panel -> weaponGroupPanelsByIndex[groupIndex] = panel },
            buildTagList = { tagListPanel, ship, groupIndex, left, right, bottom, top ->
                weaponGroupTagListRenderer.build(
                    panel = tagListPanel,
                    ship = ship,
                    groupIndex = groupIndex,
                    relativeScrollLeft = left,
                    relativeScrollRight = right,
                    relativeScrollBottom = bottom,
                    relativeScrollTop = top,
                )
            },
        )

    private fun renderConfirmationModal(panel: CustomPanelAPI) {
        if (presetWorkflowController.renderPendingModal(panel)) return
        renderExternalConfirmationModal(panel)
    }

    fun openExternalConfirmationModalOrDirty() {
        val panel = rootPanel ?: run {
            campaignScrollDirty = true
            return
        }
        if (externalConfirmationController.hasOpenModal()) {
            closeExternalConfirmationModalTargeted()
        }
        renderExternalConfirmationModal(panel)
    }

    private fun renderExternalConfirmationModal(panel: CustomPanelAPI) {
        val request = pendingConfirmationModalRequest() ?: run {
            confirmationDialogPosition = null
            return
        }
        externalConfirmationController.render(panel, request)
    }

    private fun suppressNonModalButtonHover(firstModalButtonIndex: Int) {
        suppressButtonHoverRange(0, firstModalButtonIndex)
    }

    private fun suppressButtonHoverRange(startIndex: Int, endIndex: Int) {
        if (nonModalSuppressionSnapshot != null) return
        val start = startIndex.coerceIn(0, buttons.size)
        val end = endIndex.coerceIn(start, buttons.size)
        // Modal input consumption runs after Starsector has already had a
        // chance to update hover state. Disable and mute all already-rendered
        // AGC buttons through the shared registry, not just this view's
        // ButtonBase list, because options and raw helper buttons can live in
        // separate collections.
        buttons.subList(start, end).forEach { control -> control.syncVisualCheckedToActive() }
        nonModalSuppressionSnapshot = CampaignButtonSuppression.suppressRegisteredCampaignButtonHoverSnapshot()
    }

    private fun restoreSuppressedNonModalButtonHover() {
        nonModalSuppressionSnapshot?.restore()
        nonModalSuppressionSnapshot = null
        externalBindings.restoreOptionHover?.invoke()
    }

    private fun closeExternalConfirmationModalTargeted() {
        externalConfirmationController.closeTargeted()
    }

    fun buildIn(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        buildOptionsPanel: ((CustomPanelAPI, Boolean, Int, Float?, (Int, Int) -> Unit) -> CampaignOptionsRenderResult)? = null,
        optionsPreferredHeightProvider: ((Float) -> Float)? = null,
        stableOptionsPreferredHeightProvider: ((Float) -> Float)? = null,
        buildModifiersPanel: ((CustomPanelAPI) -> Unit)? = null,
        modifiersPreferredHeightProvider: (() -> Float)? = null,
    ) {
        val startNs = System.nanoTime()
        optionsPanelController.startBuild(buildOptionsPanel)
        rootPanel = panel
        nonModalSuppressionSnapshot = null
        CampaignButtonSuppression.clearRegisteredCampaignButtons()
        buttons.clear()
        weaponGroupTagListRenderer.clear()
        weaponGroupPanelsByIndex.clear()
        weaponGroupHeadingPanelsByIndex.clear()
        shipModePanelController.clearState()
        effectiveShipModeResolver.clear()
        campaignScrollDirty = false
        presetWorkflowController.clearDirtyCache()
        activePersistenceContext = ShipEditorPersistenceContext(ship, config.runtimeShip)
        weaponGroupTagListRenderer.bindPersistenceContext(activePersistenceContext)
        activeShip = ship
        restorePersistentPresetControlStates(ship)
        confirmationDialogPosition = null
        customListModalController.resetForBuild()
        presetWorkflowController.resetModal()
        externalConfirmationController.reset()
        observedTagSelectionVersion = TagButton.campaignTagSelectionVersion
        observedShipModeSelectionVersion = ShipModeButton.campaignShipModeSelectionVersion
        activePersistenceContext?.let(ShipViewHotTagCache::ensureLoaded)

        val layout = computeShipEditorLayout(
            panel = panel,
            ship = ship,
            optionsPreferredHeightProvider = optionsPreferredHeightProvider,
            stableOptionsPreferredHeightProvider = stableOptionsPreferredHeightProvider,
            modifiersPreferredHeightProvider = modifiersPreferredHeightProvider
        )

        val leftColumnPanel = CampaignPanelFactory.addPanel(
            parent = panel,
            width = layout.leftColumnWidth,
            height = panel.position.height,
            type = CampaignPanelType.LEFT_COLUMN_PANEL,
            x = 0f,
            y = 0f
        )

        val shipPanel = CampaignPanelFactory.addPanel(
            parent = leftColumnPanel,
            width = layout.leftColumnWidth,
            height = layout.shipPanelHeight,
            type = CampaignPanelType.SHIP_PANEL,
            x = 0f,
            y = 0f
        )
        buildShipPanel(shipPanel, ship)

        val visibleSpacerHeight = if (isCollapsed(CollapsiblePanelKey.SHIP)) 0f else layout.spacerHeight
        val spacerPanel = CampaignPanelFactory.addBlackSpacerBelow(
            parent = leftColumnPanel,
            anchor = shipPanel,
            width = layout.leftColumnWidth,
            height = visibleSpacerHeight,
        )

        val optionsPanel = CampaignPanelFactory.addPanelBelow(
            parent = leftColumnPanel,
            anchor = spacerPanel ?: shipPanel,
            width = layout.leftColumnWidth,
            height = layout.optionsHeight,
            type = CampaignPanelType.OPTIONS_PANEL
        )
        val optionsPanelTop = layout.shipPanelHeight + visibleSpacerHeight
        val optionsCollapsed = isCollapsed(CollapsiblePanelKey.OPTIONS)
        if (!optionsCollapsed) {
            optionsPanelController.build(
                panel = optionsPanel,
                width = layout.leftColumnWidth,
                height = layout.optionsHeight,
                panelTop = optionsPanelTop,
                rootHeight = panel.position.height,
            )
        } else {
            optionsPanelController.clearForCollapsedPanel()
        }
        addCollapsiblePanelHeading(optionsPanel, CollapsiblePanelKey.OPTIONS)

        var leftColumnAnchor = optionsPanel
        if (buildModifiersPanel != null && layout.modifiersHeight > 0f) {
            val modifiersPanel = CampaignPanelFactory.addPanelBelow(
                parent = leftColumnPanel,
                anchor = optionsPanel,
                width = layout.leftColumnWidth,
                height = layout.modifiersHeight,
                type = CampaignPanelType.OPTIONS_PANEL
            )
            buildModifiersPanel.invoke(modifiersPanel)
            leftColumnAnchor = modifiersPanel
        }

        val shipModesPanel = CampaignPanelFactory.addPanelBelow(
            parent = leftColumnPanel,
            anchor = leftColumnAnchor,
            width = layout.leftColumnWidth,
            height = layout.shipModeHeight,
            type = CampaignPanelType.SHIP_MODES_PANEL
        )
        val shipModesPanelTop = layout.shipPanelHeight +
            visibleSpacerHeight +
            layout.optionsHeight +
            layout.modifiersHeight
        shipModePanelController.build(shipModesPanel, ship, shipModesPanelTop, panel.position.height)

        val weaponGroupsPanel = CampaignPanelFactory.addPanelRightOf(
            parent = panel,
            anchor = leftColumnPanel,
            width = layout.weaponGroupsWidth,
            height = panel.position.height,
            type = CampaignPanelType.WEAPON_GROUPS_PANEL
        )
        buildWeaponGroupsPanel(weaponGroupsPanel, ship, panel.position.height, layout.leftColumnWidth)
        if (pendingConfirmationModalRequest() != null) {
            renderConfirmationModal(panel)
        } else {
            confirmationDialogPosition = null
            customListModalController.render(panel)
        }
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
        if (elapsedMs >= CampaignGuiStyle.GUI_PERF_LOG_THRESHOLD_MS) {
            log.info("[AGC_PERF] ShipView.buildIn took ${elapsedMs}ms")
        }
    }

    private fun computeShipEditorLayout(
        panel: CustomPanelAPI,
        ship: FleetMemberAPI,
        optionsPreferredHeightProvider: ((Float) -> Float)?,
        stableOptionsPreferredHeightProvider: ((Float) -> Float)?,
        modifiersPreferredHeightProvider: (() -> Float)?,
    ): ShipEditorLayout {
        return ShipEditorLayoutCalculator.compute(
            panelWidth = panel.position.width,
            panelHeight = panel.position.height,
            ship = ship,
            shipModes = effectiveShipModesForEditor(ship),
            optionsPreferredHeightProvider = optionsPreferredHeightProvider,
            stableOptionsPreferredHeightProvider = stableOptionsPreferredHeightProvider,
            modifiersPreferredHeightProvider = modifiersPreferredHeightProvider,
            isCollapsed = ::isCollapsed,
        )
    }

    fun showShipModes(attributes: GUIAttributes) {
        val ship = attributes.ship ?: return
        val screenWidthUi = Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
        val panelHeight = Global.getSettings().screenHeightPixels / Global.getSettings().screenScaleMult - 40f
        attributes.customPanel = attributes.visualPanel?.showCustomPanel(screenWidthUi, panelHeight, this)
        attributes.customPanel?.position?.inTL(0f, 20f)
        attributes.customPanel?.let {
            buildIn(it, ship)
        }
    }

    override fun render(alpha: Float) {
        if (!config.drawFrame) return
        super.render(alpha)
    }
}
