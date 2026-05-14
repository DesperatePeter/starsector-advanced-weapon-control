package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.modals.*

import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.fs.starfarer.api.ui.CustomPanelAPI
import kotlin.math.min

internal data class PresetActionModalCallbacks(
    val setScopeWheelRegion: (Float) -> Unit,
    val updateAndRefresh: (PresetControlState) -> Unit,
    val canConfirm: () -> Boolean,
)

internal data class PresetActionModalRenderResult(
    val shell: ShipViewModalShell,
    val buttonEndIndex: Int,
)

/**
 * Renders the Save/Load confirmation modal and its option rows.
 * ShipView owns lifecycle, persistence, and post-action refresh.
 */
internal object PresetActionModalRenderer {
    fun render(
        panel: CustomPanelAPI,
        state: PresetControlState,
        request: CampaignConfirmationModalRequest,
        reviewBody: PresetActionReviewBody?,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: PresetActionModalCallbacks,
    ): PresetActionModalRenderResult {
        val overwriteWarning = CampaignSaveLoadPanelRenderer.requiresOverwriteWarning(state)
        val accentColor = if (overwriteWarning) CampaignGuiStyle.ALERT_RED_COLOR else CampaignGuiStyle.LOAD_BUTTON_HOVER_COLOR
        val titleColor = if (overwriteWarning) CampaignGuiStyle.ALERT_RED_COLOR else CampaignGuiStyle.LOAD_BUTTON_HOVER_COLOR
        val dialogWidth = min(CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH, panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING)
        val layout = PresetActionModalLayoutBuilder.build(
            screenHeight = panel.position.height,
            dialogWidth = dialogWidth,
            state = state,
            request = request,
            reviewBody = reviewBody,
        )
        val shell = ShipViewModalFactory.createShell(
            panel = panel,
            dialogWidth = dialogWidth,
            dialogHeight = layout.dialogHeight,
            borderColor = accentColor,
        )
        val dialog = shell.dialog
        renderTitle(dialog, dialogWidth, request.title, titleColor)

        val bodyTop = CampaignGuiStyle.MODAL_PADDING +
            CampaignGuiStyle.MODAL_HEADING_HEIGHT +
            CampaignGuiStyle.MODAL_TITLE_BODY_GAP
        renderBody(dialog, layout, bodyTop)
        var y = bodyTop +
            layout.bodyHeight +
            PresetActionModalLayoutBuilder.BODY_TO_CONTROLS_GAP
        callbacks.setScopeWheelRegion(y)
        renderScopeToggle(dialog, state, y, buttons, callbacks)
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        renderBackendToggle(dialog, state, y, buttons, callbacks)
        y += CampaignGuiStyle.MODAL_ROW_HEIGHT + CampaignGuiStyle.MODAL_ROW_GAP
        if (state.pendingAction == PendingPresetAction.SAVE) {
            renderOverwriteToggle(dialog, state, y, buttons, callbacks)
        }

        val canConfirm = callbacks.canConfirm()
        CustomListModalFooterRenderer.addEdgeButtons(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = layout.dialogHeight,
            buttons = buttons,
            left = ModalFooterButtonSpec(
                data = "preset_action_modal_confirm",
                kind = CampaignActionButtonKind.CONFIRM,
                enabled = canConfirm,
                labelText = "Confirm",
                tooltip = if (canConfirm) {
                    "Apply this ${state.pendingAction?.name?.lowercase() ?: "preset"} action."
                } else {
                    "No matching preset is available for these options."
                },
                showTooltipWhileInactive = true,
            ) {
                if (callbacks.canConfirm()) {
                    request.onConfirm()
                }
            },
            right = ModalFooterButtonSpec(
                data = "preset_action_modal_cancel",
                kind = CampaignActionButtonKind.CANCEL,
                labelText = "Cancel",
                tooltip = "Cancel this preset action.",
            ) {
                request.onCancel()
            },
        )
        return PresetActionModalRenderResult(shell = shell, buttonEndIndex = buttons.size)
    }

    fun stateForScopeSelection(
        state: PresetControlState,
        nextScope: WeaponPresetScope,
    ): PresetControlState {
        val nextBackend = if (
            state.pendingAction == PendingPresetAction.SAVE &&
            !state.backendManuallySelected &&
            state.scope == WeaponPresetScope.SINGLE &&
            nextScope != WeaponPresetScope.SINGLE
        ) {
            WeaponPresetBackend.EXTERNAL
        } else {
            state.backend
        }
        return state.copy(
            scope = nextScope,
            backend = nextBackend,
            overwrite = if (nextScope == WeaponPresetScope.SUGGESTED) false else state.overwrite,
        )
    }

    private fun renderTitle(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        title: String,
        color: java.awt.Color,
    ) {
        val text = dialog.createUIElement(
            dialogWidth - 2f * CampaignGuiStyle.MODAL_PADDING,
            CampaignGuiStyle.MODAL_HEADING_HEIGHT,
            false,
        )
        text.addAgcLargeHeading(title, color)
        dialog.addUIElement(text).inTL(CampaignGuiStyle.MODAL_PADDING, CampaignGuiStyle.MODAL_PADDING)
    }

    private fun renderBody(
        dialog: CustomPanelAPI,
        layout: PresetActionModalLayout,
        bodyTop: Float,
    ) {
        val reviewBody = layout.reviewBody
        if (reviewBody != null) {
            val body = dialog.createCustomPanel(layout.contentWidth, layout.bodyHeight, null)
            dialog.addComponent(body)
            body.position.inTL(CampaignGuiStyle.MODAL_PADDING, bodyTop)
            PresetActionReviewRenderer.render(
                body = body,
                review = reviewBody,
                contentWidth = layout.contentWidth,
                maxPreviewRows = layout.reviewMaxPreviewRows,
            )
        } else {
            val body = dialog.createUIElement(layout.contentWidth, layout.bodyHeight, layout.bodyScrollable)
            body.setParaFontDefault()
            renderCampaignHighlightedTextBody(body, layout.bodyParagraphs)
            dialog.addUIElement(body).inTL(CampaignGuiStyle.MODAL_PADDING, bodyTop)
        }
    }

    private fun renderScopeToggle(
        dialog: CustomPanelAPI,
        state: PresetControlState,
        y: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: PresetActionModalCallbacks,
    ) {
        CustomTagEditRowRenderer.addBidirectionalMomentaryRow(
            dialog = dialog,
            y = y,
            leftLabel = "Preset:",
            buttonText = CampaignSaveLoadPanelRenderer.scopeControlLabel(state.scope),
            kind = CampaignActionButtonKind.UNCOLOURED,
            tooltip = CampaignPresetTerminology.presetFamilyTooltip() +
                "\nLeft-click or mouse-wheel down for next; right-click or mouse-wheel up for previous.",
            buttons = buttons,
            onLeftClick = {
                selectScope(state, CampaignSaveLoadPanelRenderer.nextAvailableScope(state.scope), callbacks)
            },
            onRightClick = {
                selectScope(state, CampaignSaveLoadPanelRenderer.previousAvailableScope(state.scope), callbacks)
            },
        )
    }

    private fun selectScope(
        state: PresetControlState,
        nextScope: WeaponPresetScope,
        callbacks: PresetActionModalCallbacks,
    ) {
        callbacks.updateAndRefresh(stateForScopeSelection(state, nextScope))
    }

    private fun renderBackendToggle(
        dialog: CustomPanelAPI,
        state: PresetControlState,
        y: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: PresetActionModalCallbacks,
    ) {
        val external = state.backend == WeaponPresetBackend.EXTERNAL
        val canToggle = CampaignSaveLoadPanelRenderer.canToggleBackend(state)
        val actionText = if (state.pendingAction == PendingPresetAction.LOAD) "load" else "save"
        addToggleRow(
            dialog = dialog,
            y = y,
            leftLabel = "Cross campaign $actionText:",
            buttonText = if (external) "Enabled" else "Disabled",
            kind = when {
                !canToggle -> CampaignActionButtonKind.UNCOLOURED
                external -> CampaignActionButtonKind.CONFIRM
                else -> CampaignActionButtonKind.UNCOLOURED
            },
            tooltip = if (canToggle) {
                "Toggle cross-campaign ${state.scope.label()} preset storage."
            } else {
                "Cross-campaign storage is not available for this preset scope."
            },
            canToggle = canToggle,
            buttons = buttons,
        ) {
            val nextBackend = if (external) WeaponPresetBackend.CAMPAIGN else WeaponPresetBackend.EXTERNAL
            callbacks.updateAndRefresh(state.copy(backend = nextBackend, backendManuallySelected = true))
        }
    }

    private fun renderOverwriteToggle(
        dialog: CustomPanelAPI,
        state: PresetControlState,
        y: Float,
        buttons: MutableList<ButtonBase<*>>,
        callbacks: PresetActionModalCallbacks,
    ) {
        val canToggle = CampaignSaveLoadPanelRenderer.canToggleOverwrite(state)
        addToggleRow(
            dialog = dialog,
            y = y,
            leftLabel = "Overwrite:",
            buttonText = if (state.overwrite) "Enabled" else "Disabled",
            kind = when {
                !canToggle -> CampaignActionButtonKind.UNCOLOURED
                state.overwrite -> CampaignActionButtonKind.CANCEL
                else -> CampaignActionButtonKind.UNCOLOURED
            },
            tooltip = if (canToggle) {
                "Toggle whether saving also overwrites matching active weapon groups."
            } else {
                "Overwrite is unavailable for this preset scope."
            },
            canToggle = canToggle,
            buttons = buttons,
        ) {
            callbacks.updateAndRefresh(state.copy(overwrite = !state.overwrite))
        }
    }

    private fun addToggleRow(
        dialog: CustomPanelAPI,
        y: Float,
        leftLabel: String,
        buttonText: String,
        kind: CampaignActionButtonKind,
        tooltip: String,
        canToggle: Boolean,
        buttons: MutableList<ButtonBase<*>>,
        onToggle: () -> Unit,
    ) {
        CustomTagEditRowRenderer.addMomentaryRow(
            dialog = dialog,
            y = y,
            leftLabel = leftLabel,
            buttonText = buttonText,
            kind = kind,
            buttons = buttons,
            tooltip = tooltip,
            enabled = canToggle,
            onClick = onToggle,
        )
    }
}
