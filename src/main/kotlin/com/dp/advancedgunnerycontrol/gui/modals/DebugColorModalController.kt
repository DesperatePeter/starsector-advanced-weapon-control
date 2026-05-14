package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI
import java.awt.Color

internal object DebugColorModalController {
    fun render(
        dialog: CustomPanelAPI,
        dialogWidth: Float,
        dialogHeight: Float,
        buttons: MutableList<ButtonBase<*>>,
        state: CustomListModalStateController,
        renderTitle: (CustomPanelAPI, Float, String) -> Unit,
        onClose: () -> Unit,
    ) {
        val targets = CampaignGuiStyle.debugColorTargets()
        if (targets.isEmpty()) {
            onClose()
            return
        }

        val index = state.debugColorIndex(targets.lastIndex)
        val target = targets[index]
        ensureDraft(state, target)
        val draft = currentDraft(state, target)
        val persistent = state.debugColorPersistent()

        DebugColorModalRenderer.render(
            dialog = dialog,
            dialogWidth = dialogWidth,
            dialogHeight = dialogHeight,
            buttons = buttons,
            target = target,
            targetIndex = index,
            targetCount = targets.size,
            draft = draft,
            persistent = persistent,
            renderTitle = renderTitle,
            onCycleTarget = { delta -> selectTarget(state, targets, index, delta) },
            onDraftChanged = state::setDebugColorDraft,
            onPersistenceChanged = state::setDebugColorPersistent,
            onApply = { selectedTarget ->
                CampaignGuiStyle.setDebugColorOverride(
                    selectedTarget,
                    currentDraft(state, selectedTarget),
                    persistent = state.debugColorPersistent(),
                )
            },
            onClose = onClose,
        )
    }

    private fun selectTarget(
        state: CustomListModalStateController,
        targets: List<CampaignGuiStyle.DebugColorTarget>,
        currentIndex: Int,
        delta: Int,
    ) {
        if (targets.isEmpty()) return
        val nextIndex = (currentIndex + delta + targets.size) % targets.size
        state.setDebugColorIndex(nextIndex)
        state.setDebugColorDraft(CampaignGuiStyle.currentDebugColor(targets[nextIndex]))
    }

    private fun ensureDraft(
        state: CustomListModalStateController,
        target: CampaignGuiStyle.DebugColorTarget,
    ) {
        if (!state.hasDebugColorDraftRgb()) {
            state.setDebugColorDraft(CampaignGuiStyle.currentDebugColor(target))
        }
    }

    private fun currentDraft(
        state: CustomListModalStateController,
        target: CampaignGuiStyle.DebugColorTarget,
    ): Color {
        return state.debugColorDraft(CampaignGuiStyle.currentDebugColor(target))
    }
}
