package com.dp.advancedgunnerycontrol.gui.input

import com.dp.advancedgunnerycontrol.gui.controls.scroll.handleVerticalScrollInput
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffset
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffsetByDelta
import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.scroll.verticalScrollRegionContainsEvent

import com.dp.advancedgunnerycontrol.gui.customlists.context.*
import com.dp.advancedgunnerycontrol.gui.customlists.edit.*
import com.dp.advancedgunnerycontrol.gui.customlists.manager.*
import com.dp.advancedgunnerycontrol.gui.customlists.modal.*
import com.dp.advancedgunnerycontrol.gui.customlists.mutation.*
import com.dp.advancedgunnerycontrol.gui.customlists.review.*
import com.dp.advancedgunnerycontrol.gui.customlists.state.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.presets.*

import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lwjgl.input.Keyboard

internal object ShipViewInputController {
    fun processPanelScrollInput(
        events: MutableList<InputEventAPI>?,
        panelPos: PositionAPI?,
        region: VerticalScrollRegion<Unit>?,
        currentOffset: () -> Int,
        setOffset: (Int) -> Unit,
        onScrolled: () -> Unit,
    ): Boolean {
        val scrollRegion = region ?: return false
        return handleVerticalScrollInput(
            events = events,
            panelPos = panelPos,
            regions = listOf(scrollRegion),
            currentOffset = { currentOffset() },
            setOffset = { _, offset -> setOffset(offset) },
            onScrolled = { onScrolled() },
            scrollStep = 1,
            normalizeOffset = { _, requested, maxOffset, delta ->
                usefulVerticalScrollOffset(requested, maxOffset, delta)
            },
        )
    }

    fun processCustomListModalScrollInput(
        events: MutableList<InputEventAPI>?,
        dialogPosition: PositionAPI?,
        region: CustomListModalScrollRegion?,
        currentOffset: (CustomListModalScrollTarget) -> Int,
        setOffset: (CustomListModalScrollTarget, Int) -> Unit,
        onScrolled: () -> Unit,
    ): Boolean {
        val scrollRegion = region ?: return false
        return handleVerticalScrollInput(
            events = events,
            panelPos = dialogPosition,
            regions = listOf(scrollRegion.asVerticalScrollRegion()),
            currentOffset = currentOffset,
            setOffset = setOffset,
            onScrolled = { onScrolled() },
            scrollStep = CampaignGuiStyle.WEAPON_TAG_SCROLL_STEP,
            normalizeOffset = { _, requested, maxOffset, delta ->
                usefulVerticalScrollOffset(requested, maxOffset, delta)
            },
        )
    }

    fun scrollByRows(
        currentOffset: Int,
        delta: Int,
        maxOffset: Int,
        setOffset: (Int) -> Unit,
        onScrolled: () -> Unit,
    ) {
        val updated = usefulVerticalScrollOffsetByDelta(currentOffset, delta, maxOffset)
        if (updated == currentOffset) return
        setOffset(updated)
        onScrolled()
    }

    fun processPresetScopeWheelEvent(
        event: InputEventAPI,
        pendingPreset: Pair<Int, PresetControlState>?,
        region: VerticalScrollRegion<Unit>?,
        dialogPosition: PositionAPI?,
        updateAndRefresh: (Int, PresetControlState) -> Unit,
    ): Boolean {
        if (event.isConsumed || !event.isMouseScrollEvent) return false
        val eventValue = event.eventValue
        if (eventValue == 0) return false
        val (groupIndex, state) = pendingPreset ?: return false
        val scrollRegion = region ?: return false
        if (!verticalScrollRegionContainsEvent(event, dialogPosition, scrollRegion)) return false
        val nextScope = if (eventValue > 0) {
            CampaignSaveLoadPanelRenderer.previousAvailableScope(state.scope)
        } else {
            CampaignSaveLoadPanelRenderer.nextAvailableScope(state.scope)
        }
        event.consume()
        updateAndRefresh(
            groupIndex,
            PresetActionModalRenderer.stateForScopeSelection(state, nextScope),
        )
        return true
    }

    fun processCustomListModalInput(
        events: MutableList<InputEventAPI>?,
        mode: CustomListModalMode?,
        isManagerReturnEdit: () -> Boolean,
        returnToManagerFromEdit: () -> Unit,
        closeModal: () -> Unit,
        processRightClickEvent: (InputEventAPI) -> Boolean,
        currentLoadoutRenameIndex: () -> Int,
        currentLoadoutRenameDraft: () -> String,
        setLoadoutRenameDraft: (String) -> Unit,
    ): Boolean {
        val currentMode = mode ?: return false
        events?.forEach { event ->
            if (event.isConsumed) return@forEach
            if (event.isKeyDownEvent) {
                if (currentMode == CustomListModalMode.RENAME_LOADOUT) {
                    processLoadoutRenameKeyEvent(
                        event = event,
                        index = currentLoadoutRenameIndex(),
                        current = currentLoadoutRenameDraft(),
                        setDraft = setLoadoutRenameDraft,
                        closeModal = closeModal,
                    )
                    event.consume()
                    return@forEach
                }
                if (event.eventValue == Keyboard.KEY_ESCAPE) {
                    if (currentMode == CustomListModalMode.EDIT_TAG && isManagerReturnEdit()) {
                        returnToManagerFromEdit()
                    } else {
                        closeModal()
                    }
                }
                event.consume()
                return@forEach
            }
            if (event.isMouseEvent) {
                if (processRightClickEvent(event)) return@forEach
                event.consume()
            }
        }
        return true
    }

    private fun processLoadoutRenameKeyEvent(
        event: InputEventAPI,
        index: Int,
        current: String,
        setDraft: (String) -> Unit,
        closeModal: () -> Unit,
    ) {
        when (event.eventValue) {
            Keyboard.KEY_ESCAPE -> closeModal()
            Keyboard.KEY_RETURN,
            Keyboard.KEY_NUMPADENTER -> {
                if (Settings.renameLoadout(index, current)) {
                    closeModal()
                }
            }
            Keyboard.KEY_BACK -> setDraft(current.dropLast(1))
            else -> {
                val char = event.eventChar
                if (char.code in 32..126 && current.length < 24) {
                    setDraft(current + char)
                }
            }
        }
    }
}
