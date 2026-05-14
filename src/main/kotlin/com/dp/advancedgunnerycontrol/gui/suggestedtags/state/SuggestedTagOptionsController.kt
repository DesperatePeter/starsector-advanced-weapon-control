package com.dp.advancedgunnerycontrol.gui.suggestedtags.state

import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.scroll.handleVerticalScrollInput
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffset
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffsetByDelta
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.SuggestedTagUiAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.actions.buildSuggestedTagFilterActions
import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.renderSuggestedOptionsPanel
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

internal class SuggestedTagOptionsController(
    private val weaponListView: WeaponListView,
    private val sessionStateProvider: () -> SuggestedTagSessionState,
    private val updateSessionState: (SuggestedTagSessionState) -> Unit,
) {
    private val collapsedFilterCategories = WeaponFilter.filterCategories.toMutableSet()
    private var actionScrollRegion: VerticalScrollRegion<Unit>? = null
    private var filterScrollRegion: VerticalScrollRegion<Unit>? = null

    init {
        weaponListView.replaceFilters(SuggestedTagEditorPreferences.activeFilters())
    }

    fun clearFilters() {
        weaponListView.clearFilters()
        collapsedFilterCategories.clear()
        collapsedFilterCategories.addAll(WeaponFilter.filterCategories)
        updateSessionState(sessionStateProvider().withFilterScrollOffset(0))
        SuggestedTagEditorPreferences.saveFilters(emptyList())
    }

    fun renderOptionsPanel(
        panel: CustomPanelAPI,
        currentActions: List<SuggestedTagUiAction>,
        bindButton: (SuggestedTagUiAction, ButtonAPI, Boolean) -> Unit,
    ) {
        val sessionState = sessionStateProvider()
        val result = renderSuggestedOptionsPanel(
            panel = panel,
            actions = currentActions,
            filterRows = buildSuggestedTagFilterActions(
                weaponListView,
                collapsedFilterCategories,
                onToggleFilter = ::toggleFilter,
            ),
            pageString = weaponListView.pageString,
            actionScrollOffset = sessionState.actionScrollOffset,
            filterScrollOffset = sessionState.filterScrollOffset,
            bindButton = bindButton,
            onActionScrollIndicator = { delta, maxOffset ->
                val offset = usefulVerticalScrollOffsetByDelta(sessionStateProvider().actionScrollOffset, delta, maxOffset)
                updateSessionState(sessionStateProvider().withActionScrollOffset(offset))
            },
            onFilterScrollIndicator = { delta, maxOffset ->
                val offset = usefulVerticalScrollOffsetByDelta(sessionStateProvider().filterScrollOffset, delta, maxOffset)
                updateSessionState(sessionStateProvider().withFilterScrollOffset(offset))
            },
        )
        updateSessionState(sessionStateProvider().withActionScrollOffset(result.actionScrollOffset))
        updateSessionState(sessionStateProvider().withFilterScrollOffset(result.filterScrollOffset))
        actionScrollRegion = result.actionScrollRegion
        filterScrollRegion = result.filterScrollRegion
    }

    fun processScrollInput(
        events: MutableList<InputEventAPI>,
        panelPos: PositionAPI?,
        onScrolled: () -> Unit,
    ): Boolean {
        if (processOptionScrollInput(
                events = events,
                panelPos = panelPos,
                onScrolled = onScrolled,
                regionProvider = { actionScrollRegion },
                currentOffsetProvider = { sessionStateProvider().actionScrollOffset },
                updateOffset = { offset ->
                    updateSessionState(sessionStateProvider().withActionScrollOffset(offset))
                },
            )
        ) return true
        return processOptionScrollInput(
            events = events,
            panelPos = panelPos,
            onScrolled = onScrolled,
            regionProvider = { filterScrollRegion },
            currentOffsetProvider = { sessionStateProvider().filterScrollOffset },
            updateOffset = { offset ->
                updateSessionState(sessionStateProvider().withFilterScrollOffset(offset))
            },
        )
    }

    private fun toggleFilter(filter: WeaponFilter) {
        weaponListView.toggleFilter(filter)
        SuggestedTagEditorPreferences.saveFilters(weaponListView.activeFilters())
    }

    private fun processOptionScrollInput(
        events: MutableList<InputEventAPI>,
        panelPos: PositionAPI?,
        onScrolled: () -> Unit,
        regionProvider: () -> VerticalScrollRegion<Unit>?,
        currentOffsetProvider: () -> Int,
        updateOffset: (Int) -> Unit,
    ): Boolean {
        val region = regionProvider() ?: return false
        return handleVerticalScrollInput(
            events = events,
            panelPos = panelPos,
            regions = listOf(region),
            currentOffset = { currentOffsetProvider() },
            setOffset = { _, offset ->
                updateOffset(offset)
            },
            onScrolled = { onScrolled() },
            scrollStep = CampaignGuiStyle.SUGGESTED_FILTER_SCROLL_STEP,
            normalizeOffset = { _, requested, maxOffset, delta ->
                usefulVerticalScrollOffset(requested, maxOffset, delta)
            },
        )
    }
}
