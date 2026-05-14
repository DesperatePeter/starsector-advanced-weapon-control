package com.dp.advancedgunnerycontrol.gui.shipview

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
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

import com.dp.advancedgunnerycontrol.gui.modals.DebugColorModalController
import com.dp.advancedgunnerycontrol.gui.modals.LoadoutRenameModalRenderer
import com.dp.advancedgunnerycontrol.gui.session.CustomListModalBindings
import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.foundation.addAgcLargeHeading
import com.dp.advancedgunnerycontrol.gui.input.ShipViewInputController
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.min

internal class ShipViewCustomListModalController(
    private val bindings: CustomListModalBindings,
    private val buttons: MutableList<ButtonBase<*>>,
    private val activeShip: () -> FleetMemberAPI?,
    private val setActiveShip: (FleetMemberAPI) -> Unit,
    private val activePersistenceContext: () -> ShipEditorPersistenceContext?,
    private val loadoutIndex: () -> Int,
    private val processRightClickEvent: (InputEventAPI) -> Boolean,
    private val suppressNonModalButtonHover: (Int) -> Unit,
    private val restoreSuppressedNonModalButtonHover: () -> Unit,
    private val onMutationFinished: (FleetMemberAPI) -> Unit,
    private val markDirty: () -> Unit,
) : CustomListModalChangeHandler {
    private val state = CustomListModalStateController(
        bindings,
        changeHandler = this,
    )
    private val contextController = CustomListContextController(state)
    private val shell = CustomListModalShellController(buttons)
    private val mutations = CustomListMutationController(
        state = state,
        contextController = contextController,
        editSource = {
            val groupIndex = bindings.editSourceGroupProvider?.invoke()
            val tag = bindings.editSourceTagProvider?.invoke()?.takeIf { it.isNotBlank() }
            if (groupIndex != null && tag != null) groupIndex to tag else null
        },
        activeShip = activeShip,
        onMutationFinished = ::finishMutation,
    )
    private val editController = CustomTagEditController(
        state = state,
        contextController = contextController,
        bindings = bindings,
        buttons = buttons,
        activePersistenceContext = activePersistenceContext,
        renderTitle = { dialog, width, title -> renderTitle(dialog, width, title) },
        closeModal = ::close,
        addCustomTag = mutations::addCustomTag,
        replaceCustomTag = mutations::replaceCustomTag,
        removeCustomTags = mutations::removeCustomTags,
        addCustomShipMode = mutations::addCustomShipMode,
        replaceCustomShipMode = mutations::replaceCustomShipMode,
        removeCustomShipModes = mutations::removeCustomShipModes,
    )

    private var rendering = false
    private var refreshPending = false
    private var scrollRegion: CustomListModalScrollRegion? = null

    val dialogPosition get() = shell.dialogPosition

    fun hasModal(): Boolean = bindings.modalModeProvider?.invoke() != null

    fun resetForBuild() {
        scrollRegion = null
        shell.reset()
        rendering = false
        refreshPending = false
    }

    fun processScrollInput(events: MutableList<InputEventAPI>?): Boolean {
        return ShipViewInputController.processCustomListModalScrollInput(
            events = events,
            dialogPosition = shell.dialogPosition,
            region = scrollRegion,
            currentOffset = ::scrollOffset,
            setOffset = ::setScrollOffset,
            onScrolled = ::refreshOrDirty,
        )
    }

    fun processModalInput(events: MutableList<InputEventAPI>?): Boolean {
        return ShipViewInputController.processCustomListModalInput(
            events = events,
            mode = bindings.modalModeProvider?.invoke(),
            isManagerReturnEdit = state::isManagerReturnEdit,
            returnToManagerFromEdit = editController::returnToManagerFromEdit,
            closeModal = ::close,
            processRightClickEvent = processRightClickEvent,
            currentLoadoutRenameIndex = ::currentLoadoutRenameIndex,
            currentLoadoutRenameDraft = ::currentLoadoutRenameDraft,
            setLoadoutRenameDraft = ::setLoadoutRenameDraft,
        )
    }

    fun processVisibleWeaponTagRightClickEdit(events: MutableList<InputEventAPI>?): Boolean {
        val ship = activeShip() ?: return false
        val context = activePersistenceContext() ?: return false
        if (context.shipId.isBlank()) return false
        if (!CustomWeaponTagListStore.getActiveModeOrDefault(context.shipId).isCustom) return false
        val rightClickEvents = events
            ?.filter { event -> !event.isConsumed && event.isRMBDownEvent }
            .orEmpty()
        if (rightClickEvents.isEmpty()) return false

        var customTags: Set<String>? = null
        rightClickEvents.forEach { event ->
            for (button in buttons) {
                val candidate = button as? TagButton ?: continue
                if (!candidate.containsEvent(event)) continue
                val canEdit = candidate.isEditableTag() || run {
                    val tags = customTags ?: contextController.currentTagsFor(context)
                        .toSet()
                        .also { customTags = it }
                    canonicalTag(candidate.associatedValue) in tags
                }
                if (!canEdit) return@forEach
                event.consume()
                CustomTagEditLauncher.startVisibleWeaponTagEdit(state, candidate.group, candidate.associatedValue)
                setActiveShip(ship)
                return true
            }
        }
        return false
    }

    fun processVisibleShipModeRightClickEdit(events: MutableList<InputEventAPI>?): Boolean {
        val ship = activeShip() ?: return false
        val context = activePersistenceContext() ?: return false
        if (context.shipId.isBlank()) return false
        if (!CustomWeaponTagListStore.getActiveModeOrDefault(context.shipId).isCustom) return false
        val rightClickEvents = events
            ?.filter { event -> !event.isConsumed && event.isRMBDownEvent }
            .orEmpty()
        if (rightClickEvents.isEmpty()) return false

        rightClickEvents.forEach { event ->
            for (button in buttons) {
                val candidate = button as? ShipModeButton ?: continue
                if (!candidate.containsEvent(event)) continue
                val mode = candidate.associatedValue
                if (!CustomTagEditLauncher.startVisibleShipModeEdit(state, mode)) return@forEach
                event.consume()
                setActiveShip(ship)
                return true
            }
        }
        return false
    }

    fun render(panel: CustomPanelAPI) {
        val mode = bindings.modalModeProvider?.invoke() ?: run {
            shell.reset()
            scrollRegion = null
            return
        }
        val ship = activeShip() ?: return
        val context = activePersistenceContext() ?: return
        val firstModalButtonIndex = buttons.size
        suppressNonModalButtonHover(firstModalButtonIndex)
        scrollRegion = null
        rendering = true
        refreshPending = false
        val nestedManagerEdit = mode == CustomListModalMode.EDIT_TAG && state.isManagerReturnEdit()
        val managerRows = if (mode == CustomListModalMode.MANAGE_TAGS || nestedManagerEdit) {
            contextController.managerRowsFor(context)
        } else {
            emptyList()
        }
        val changeReviewRows = if (mode == CustomListModalMode.CONFIRM_TAG_CHANGES) {
            contextController.changeReviewRowsFor(context)
        } else {
            emptyList()
        }

        val targetWidth = CustomListModalLayout.targetWidth(mode, nestedManagerEdit)
        val targetHeight = CustomListModalLayout.targetHeight(
            mode = mode,
            nestedManagerEdit = nestedManagerEdit,
            editParameterCount = editController.currentParameterCount(),
            managerRowCount = managerRows.size,
            changeReviewRowCount = changeReviewRows.size,
        )
        val dialogWidth = min(targetWidth, panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING)
        val dialogHeight = min(targetHeight, CustomListModalLayout.maxModalHeight(panel.position.height))
        val renderedShell = shell.createShell(
            panel = panel,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            firstModalButtonIndex = firstModalButtonIndex,
        )
        val dialog = renderedShell.dialog

        when (mode) {
            CustomListModalMode.MANAGE_TAGS -> {
                scrollRegion = CustomListManagerModalRenderer.renderManager(
                    dialog = dialog,
                    ship = ship,
                    context = context,
                    dialogWidth = dialogWidth,
                    dialogHeight = dialogHeight,
                    rows = managerRows,
                    state = state,
                    buttons = buttons,
                    callbacks = managerCallbacks(),
                )
            }
            CustomListModalMode.EDIT_TAG -> {
                if (nestedManagerEdit) {
                    scrollRegion = CustomListManagerModalRenderer.renderNestedManagerEdit(
                        dialog = dialog,
                        ship = ship,
                        context = context,
                        dialogWidth = dialogWidth,
                        dialogHeight = dialogHeight,
                        rows = managerRows,
                        editParameterCount = editController.currentParameterCount(),
                        state = state,
                        buttons = buttons,
                        callbacks = managerCallbacks(),
                    )
                } else {
                    editController.renderParameterEditor(dialog, ship, context, dialogWidth, dialogHeight)
                }
            }
            CustomListModalMode.CONFIRM_TAG_CHANGES -> {
                scrollRegion = CustomListChangeReviewModalRenderer.renderChangeConfirmation(
                    dialog = dialog,
                    ship = ship,
                    context = context,
                    dialogWidth = dialogWidth,
                    dialogHeight = dialogHeight,
                    rows = changeReviewRows,
                    hasRemovals = contextController.hasPendingRemovals(context),
                    state = state,
                    buttons = buttons,
                    callbacks = managerCallbacks(),
                )
            }
            CustomListModalMode.DEBUG_COLORS -> renderDebugColorModal(dialog, dialogWidth, dialogHeight)
            CustomListModalMode.RENAME_LOADOUT -> renderLoadoutRenameModal(dialog, dialogWidth, dialogHeight)
        }
        shell.finishRender()
        rendering = false
        if (refreshPending) {
            refreshPending = false
            markDirty()
        }
    }

    override fun onCustomListModalStateChanged() {
        if (rendering) {
            refreshPending = true
            return
        }
        val mode = bindings.modalModeProvider?.invoke()
        if (mode == null) {
            markDirty()
            return
        }
        refreshOrDirty()
    }

    fun refreshOrDirty() {
        val panel = shell.detachAndClear()
        if (panel == null) {
            markDirty()
            return
        }
        render(panel)
    }

    fun close() {
        state.closeModal()
        scrollRegion = null
        if (shell.detachAndClear() != null) {
            restoreSuppressedNonModalButtonHover()
        } else {
            markDirty()
        }
    }

    private fun managerCallbacks(): CustomListManagerModalCallbacks =
        CustomListManagerModalCallbacks(
            renderEditor = editController::renderParameterEditor,
            addDialogShield = shell::addBackdropButton,
            currentTags = contextController::currentTagsFor,
            currentShipModes = contextController::currentShipModesFor,
            scrollOffset = ::scrollOffset,
            setScrollOffset = ::setScrollOffset,
            refresh = ::refreshOrDirty,
            close = ::close,
            switchMode = { mode -> bindings.onModalModeUpdate?.invoke(mode) },
            applyChanges = mutations::applyManagerChanges,
            startDefinitionEdit = editController::startManagerEdit,
            startShipModeDefinitionEdit = editController::startManagerShipModeEdit,
            startTagEdit = { ship, _, tag, pendingAddition, sourceTag ->
                if (CustomTagEditLauncher.startManagerTagEdit(
                    state = state,
                    tag = tag,
                    pendingAddition = pendingAddition,
                    sourceTag = sourceTag,
                )) {
                    setActiveShip(ship)
                }
            },
            startShipModeEdit = { mode, pendingAddition, sourceMode ->
                CustomTagEditLauncher.startManagerShipModeEdit(
                    state = state,
                    mode = mode,
                    pendingAddition = pendingAddition,
                    sourceMode = sourceMode,
                )
            },
        )

    private fun canonicalTag(tag: String): String = canonicalizeWeaponTagName(tag)

    private fun scrollOffset(target: CustomListModalScrollTarget): Int {
        return when (target) {
            CustomListModalScrollTarget.MANAGE_TAGS -> bindings.managerScrollOffsetProvider?.invoke() ?: 0
            CustomListModalScrollTarget.REVIEW_CHANGES -> bindings.changeReviewScrollOffsetProvider?.invoke() ?: 0
        }
    }

    private fun setScrollOffset(target: CustomListModalScrollTarget, offset: Int) {
        when (target) {
            CustomListModalScrollTarget.MANAGE_TAGS -> bindings.onManagerScrollOffsetUpdate?.invoke(offset)
            CustomListModalScrollTarget.REVIEW_CHANGES -> bindings.onChangeReviewScrollOffsetUpdate?.invoke(offset)
        }
    }

    private fun renderLoadoutRenameModal(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
    ) {
        val index = currentLoadoutRenameIndex()
        val draft = currentLoadoutRenameDraft()
        val validation = Settings.validateLoadoutName(index, draft)
        LoadoutRenameModalRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            loadoutIndex = index,
            loadoutCount = Settings.maxLoadouts(),
            draft = draft,
            validation = validation,
            renderTitle = ::renderTitle,
            onConfirm = {
                if (Settings.renameLoadout(index, currentLoadoutRenameDraft())) {
                    close()
                    markDirty()
                }
            },
            onClose = ::close,
        )
    }

    private fun renderDebugColorModal(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
    ) {
        DebugColorModalController.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            state = state,
            renderTitle = ::renderTitle,
            onClose = ::close,
        )
    }

    private fun currentLoadoutRenameIndex(): Int {
        return state.loadoutRenameIndex(loadoutIndex(), Settings.maxLoadouts())
    }

    private fun currentLoadoutRenameDraft(): String {
        return state.loadoutRenameDraft(
            defaultIndex = loadoutIndex(),
            maxLoadouts = Settings.maxLoadouts(),
            displayName = Settings::loadoutDisplayName,
        )
    }

    private fun setLoadoutRenameDraft(name: String) {
        state.setLoadoutRenameDraft(currentLoadoutRenameIndex(), name)
    }

    private fun finishMutation(ship: FleetMemberAPI) {
        setActiveShip(ship)
        onMutationFinished(ship)
        close()
        markDirty()
    }

    private fun renderTitle(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        title: String,
        color: java.awt.Color = CampaignGuiStyle.SAVE_BUTTON_HOVER_COLOR,
    ) {
        val text = dialog.createUIElement(
            dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            CampaignGuiStyle.MODAL_HEADING_HEIGHT,
            false
        )
        text.addAgcLargeHeading(title, color)
        dialog.addUIElement(text).inTL(CampaignGuiStyle.MODAL_PADDING, CampaignGuiStyle.MODAL_PADDING)
    }
}
