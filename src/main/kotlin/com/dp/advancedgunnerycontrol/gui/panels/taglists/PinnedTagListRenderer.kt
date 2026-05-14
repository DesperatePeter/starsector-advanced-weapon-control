package com.dp.advancedgunnerycontrol.gui.panels.taglists

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.scroll.CampaignTagScrollButtons
import com.dp.advancedgunnerycontrol.gui.controls.scroll.computePinnedVerticalScrollLayout
import com.dp.advancedgunnerycontrol.gui.controls.scroll.handleVerticalScrollInput
import com.dp.advancedgunnerycontrol.gui.controls.scroll.usefulVerticalScrollOffsetByDelta
import com.dp.advancedgunnerycontrol.gui.controls.scroll.VerticalScrollRegion
import com.dp.advancedgunnerycontrol.gui.controls.scroll.visibleScrollPageDelta
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleHeading

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import kotlin.math.min

private typealias SelectedTagsProvider<Context> = (Context) -> List<String>
private typealias AvailableTagsProvider<Context> = (Context) -> List<String>
private typealias SanitizedSelectedTagsProvider<Context> = (Context, List<String>) -> List<String>
private typealias UnavailableTagsProvider<Context> = (Context, List<String>, List<String>) -> Set<String>
private typealias RemoveTagButtons<Context> = (Context) -> Unit
private typealias TagButtonRenderer<Context> =
    (Context, CustomPanelAPI, List<String>, Boolean, List<String>, List<String>, Set<String>, () -> Unit) -> Unit
private typealias ScrollIndicatorButtonRegistrar<Context> = (Context, ButtonBase<*>) -> Unit

/**
 * Shared tag-list component used by weapon groups and Customize Suggested Tags.
 * Keeps active tags pinned above inactive rows, renders tag/category headings,
 * and owns scroll indicators for tag-like lists.
 */
class PinnedTagListRenderer<ListKey : Any, Context>(
    initialScrollOffsets: Map<ListKey, Int>,
    initialExpandedCategoryTitles: Map<ListKey, Set<String>> = emptyMap(),
    private val scrollStep: Int = 2,
    private val selectedTags: SelectedTagsProvider<Context>,
    private val availableTags: AvailableTagsProvider<Context> = { Settings.getCurrentWeaponTagList() },
    private val sanitizedSelectedTags: SanitizedSelectedTagsProvider<Context> = { context, _ -> selectedTags(context) },
    private val unavailableTags: UnavailableTagsProvider<Context> = { _, _, _ -> emptySet() },
    private val removeButtons: RemoveTagButtons<Context>,
    private val addTagButtons: TagButtonRenderer<Context>,
    private val addScrollIndicatorButton: ScrollIndicatorButtonRegistrar<Context> = { _, _ -> },
    private val removeScrollIndicatorButton: ScrollIndicatorButtonRegistrar<Context> = { _, _ -> },
    private val onMissingRefreshContext: () -> Unit = {},
    private val enableCategoryHeadings: Boolean = true,
) {
    companion object {
        private const val TAG_ELLIPSIS_HEIGHT = CampaignGuiStyle.TAG_ITEM_HEIGHT
        private const val ACTIVE_TAG_SECTION = "Active Tags"
        private const val INACTIVE_TAG_SECTION = "Inactive Tags"
    }

    private sealed class TagListRow {
        data class SectionHeading(
            val title: String,
            val expanded: Boolean,
            val active: Boolean,
        ) : TagListRow()

        data class Heading(
            val category: WeaponTagCategory,
            val expanded: Boolean,
            val active: Boolean,
        ) : TagListRow()

        data class Tag(
            val tag: String,
            val selected: Boolean,
        ) : TagListRow()
    }

    private data class PanelContext<ListKey : Any, Context>(
        val key: ListKey,
        val data: Context,
        val panel: CustomPanelAPI,
        val relativeScrollLeft: Float,
        val relativeScrollRight: Float,
        val relativeScrollBottom: Float,
        val relativeScrollTop: Float,
        val children: MutableList<CustomPanelAPI> = mutableListOf(),
    )

    private val scrollOffsets = initialScrollOffsets.toMutableMap()
    private val scrollRegions = mutableListOf<VerticalScrollRegion<ListKey>>()
    private val panelContexts = mutableMapOf<ListKey, PanelContext<ListKey, Context>>()
    private val listOwnedButtonsByKey = mutableMapOf<ListKey, MutableList<ButtonBase<*>>>()
    private val expandedCategoryTitlesByKey = mutableExpandedCategoryTitles(initialExpandedCategoryTitles)
    private val collapsedTopLevelSectionsByKey = mutableMapOf<ListKey, MutableSet<String>>()

    fun clear() {
        scrollRegions.clear()
        panelContexts.clear()
        listOwnedButtonsByKey.clear()
        collapsedTopLevelSectionsByKey.values.forEach { sections ->
            sections.remove(ACTIVE_TAG_SECTION)
        }
    }

    fun captureScrollOffsets(): Map<ListKey, Int> = scrollOffsets.toMap()

    fun captureExpandedCategoryTitles(): Map<ListKey, Set<String>> {
        val captured = mutableMapOf<ListKey, Set<String>>()
        expandedCategoryTitlesByKey.forEach { (key, titles) ->
            captured[key] = titles.toSet()
        }
        return captured
    }

    fun processInput(events: MutableList<InputEventAPI>?, panelPos: PositionAPI?): Boolean {
        return handleVerticalScrollInput(
            events = events,
            panelPos = panelPos,
            regions = scrollRegions,
            currentOffset = { key -> scrollOffsets[key] ?: 0 },
            setOffset = { key, offset -> scrollOffsets[key] = offset },
            onScrolled = ::refresh,
            scrollStep = scrollStep,
        )
    }

    fun build(
        key: ListKey,
        data: Context,
        panel: CustomPanelAPI,
        relativeScrollLeft: Float,
        relativeScrollRight: Float,
        relativeScrollBottom: Float,
        relativeScrollTop: Float,
    ) {
        val context = PanelContext(
            key = key,
            data = data,
            panel = panel,
            relativeScrollLeft = relativeScrollLeft,
            relativeScrollRight = relativeScrollRight,
            relativeScrollBottom = relativeScrollBottom,
            relativeScrollTop = relativeScrollTop,
        )
        panelContexts[key] = context
        render(context)
    }

    private fun refresh(key: ListKey) {
        val context = panelContexts[key] ?: run {
            onMissingRefreshContext()
            return
        }
        render(context)
    }

    fun refreshVisibleList(key: ListKey) {
        refresh(key)
    }

    private fun render(context: PanelContext<ListKey, Context>) {
        clearRenderedState(context)

        val panel = context.panel
        val allTags = availableTags(context.data)
        val selectedRequested = sanitizedSelectedTags(context.data, allTags).toHashSet()
        val selected = allTags.filter { it in selectedRequested }
        val selectedSet = selected.toSet()
        val unselected = allTags.filterNot { it in selectedSet }
        val unavailable = unavailableTags(context.data, unselected, selected)
        val scrollRows = tagRowsFor(context.key, selected, unselected)
        val layout = computePinnedVerticalScrollLayout(
            pinnedCandidates = emptyList(),
            scrollCandidates = scrollRows,
            containerHeight = panel.position.height,
            currentOffset = scrollOffsets[context.key] ?: 0,
            indicatorHeight = TAG_ELLIPSIS_HEIGHT,
        )
        val slice = layout.scrollSlice
        scrollOffsets[context.key] = slice.offset

        if (slice.hasAbove) {
            context.children.add(
                buildScrollIndicator(
                    context = context,
                    top = layout.pinnedHeight + layout.pinnedScrollGap,
                    symbol = CampaignGuiStyle.SCROLL_INDICATOR_ABOVE,
                    pageDelta = -visibleScrollPageDelta(slice)
                )
            )
        }

        val tagPanelTop = layout.pinnedHeight +
            layout.pinnedScrollGap +
            layout.topIndicatorHeight +
            if (slice.hasAbove) CampaignGuiStyle.TAG_ITEM_VGAP else 0f
        val tagPanelHeight = min(layout.visibleScrollAreaHeight, layout.renderedScrollItemsHeight)
        val tagPanel = panel.createCustomPanel(
            panel.position.width,
            tagPanelHeight,
            null
        )
        panel.addComponent(tagPanel)
        tagPanel.position.inTL(0f, tagPanelTop)
        context.children.add(tagPanel)
        addTagRows(context, tagPanel, slice.items, allTags, selected, unavailable)

        if (slice.hasBelow) {
            context.children.add(
                buildScrollIndicator(
                    context = context,
                    top = tagPanelTop + tagPanelHeight + if (slice.items.isNotEmpty()) CampaignGuiStyle.TAG_ITEM_VGAP else 0f,
                    symbol = CampaignGuiStyle.SCROLL_INDICATOR_BELOW,
                    pageDelta = visibleScrollPageDelta(slice)
                )
            )
        }

        scrollRegions.add(
            VerticalScrollRegion(
                key = context.key,
                left = context.relativeScrollLeft,
                right = context.relativeScrollRight,
                bottom = context.relativeScrollBottom,
                top = context.relativeScrollTop,
                maxOffset = slice.maxOffset,
            )
        )
    }

    private fun tagRowsFor(key: ListKey, selectedTags: List<String>, unselectedTags: List<String>): List<TagListRow> {
        val rows = mutableListOf<TagListRow>()
        val activeExpanded = isTopLevelSectionExpanded(key, ACTIVE_TAG_SECTION)
        rows += TagListRow.SectionHeading(
            title = ACTIVE_TAG_SECTION,
            expanded = activeExpanded,
            active = selectedTags.isNotEmpty(),
        )
        if (activeExpanded) {
            rows += selectedTags.map { TagListRow.Tag(it, selected = true) }
        }

        val inactiveExpanded = isTopLevelSectionExpanded(key, INACTIVE_TAG_SECTION)
        rows += TagListRow.SectionHeading(
            title = INACTIVE_TAG_SECTION,
            expanded = inactiveExpanded,
            active = false,
        )
        if (!inactiveExpanded) return rows
        if (!enableCategoryHeadings) {
            rows += unselectedTags.map { TagListRow.Tag(it, selected = false) }
            return rows
        }
        val expandedCategoryTitles = expandedCategoryTitlesByKey[key] ?: emptySet()
        WeaponTagCategory.DISPLAY_CATEGORIES.forEach { category ->
            val categoryTags = category.sortTags(unselectedTags.filter { WeaponTagCategory.categoryFor(it) == category })
            if (categoryTags.isEmpty()) return@forEach
            val expanded = category.title in expandedCategoryTitles
            rows += TagListRow.Heading(category, expanded, active = false)
            if (expanded) {
                rows += categoryTags.map { TagListRow.Tag(it, selected = false) }
            }
        }
        return rows
    }

    private fun isTopLevelSectionExpanded(key: ListKey, title: String): Boolean =
        title !in collapsedTopLevelSectionsByKey[key].orEmpty()

    private fun addTagRows(
        context: PanelContext<ListKey, Context>,
        panel: CustomPanelAPI,
        rows: List<TagListRow>,
        allTags: List<String>,
        selectedTags: List<String>,
        unavailableTags: Set<String>,
    ) {
        rows.forEachIndexed { index, row ->
            val top = index * (CampaignGuiStyle.TAG_ITEM_HEIGHT + CampaignGuiStyle.TAG_ITEM_VGAP)
            when (row) {
                is TagListRow.SectionHeading -> addTopLevelHeadingRow(context, panel, row, top)
                is TagListRow.Heading -> addCategoryHeadingRow(context, panel, row, top)
                is TagListRow.Tag -> {
                    val rowPanel = panel.createCustomPanel(
                        panel.position.width,
                        CampaignGuiStyle.TAG_ITEM_HEIGHT,
                        null
                    )
                    panel.addComponent(rowPanel)
                    rowPanel.position.inTL(0f, top)
                    addTagButtons(context.data, rowPanel, listOf(row.tag), row.selected, allTags, selectedTags, unavailableTags) {
                        refresh(context.key)
                    }
                }
            }
        }
    }

    private fun addTopLevelHeadingRow(
        context: PanelContext<ListKey, Context>,
        panel: CustomPanelAPI,
        row: TagListRow.SectionHeading,
        top: Float,
    ) {
        val heading = CampaignToggleHeading(
            title = row.title,
            expanded = row.expanded,
            active = row.active,
            subject = "section",
            inactiveKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
            activeKind = CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION,
        )
        val button = CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = panel,
            data = "${context.key}:tag_section:${row.title}",
            x = 0f,
            y = top,
            width = panel.position.width,
            height = CampaignGuiStyle.TAG_ITEM_HEIGHT,
            template = CampaignGuiStyle.actionButtonTemplate(heading.kind),
            labelText = heading.label,
            tooltip = heading.tooltip,
            centerText = true,
        ) {
            val collapsedSections = collapsedTopLevelSectionsByKey.getOrPut(context.key) { mutableSetOf() }
            if (row.expanded) {
                collapsedSections.add(row.title)
            } else {
                collapsedSections.remove(row.title)
            }
            refresh(context.key)
        }
        listOwnedButtonsByKey.getOrPut(context.key) { mutableListOf() }.add(button)
        addScrollIndicatorButton(context.data, button)
    }

    private fun addCategoryHeadingRow(
        context: PanelContext<ListKey, Context>,
        panel: CustomPanelAPI,
        row: TagListRow.Heading,
        top: Float,
    ) {
        val heading = CampaignToggleHeading(
            title = row.category.title,
            expanded = row.expanded,
            active = row.active,
            subject = "weapon tags",
            inactiveKind = CampaignActionButtonKind.TAG_CATEGORY,
            activeKind = CampaignActionButtonKind.TAG_CATEGORY_ACTIVE,
        )
        val template = CampaignGuiStyle.actionButtonTemplate(heading.kind)
        val button = CampaignActionRows.addTemplatedCampaignMomentaryActionButton(
            parent = panel,
            data = "${context.key}:tag_category:${row.category.title}",
            x = 0f,
            y = top,
            width = panel.position.width,
            height = CampaignGuiStyle.TAG_ITEM_HEIGHT,
            template = template,
            labelText = heading.label,
            tooltip = heading.tooltip,
            centerText = true,
        ) {
            val expandedCategoryTitles = expandedCategoryTitlesByKey.getOrPut(context.key) { mutableSetOf() }
            if (row.expanded) {
                expandedCategoryTitles.remove(row.category.title)
            } else {
                expandedCategoryTitles.add(row.category.title)
            }
            refresh(context.key)
        }
        listOwnedButtonsByKey.getOrPut(context.key) { mutableListOf() }.add(button)
        addScrollIndicatorButton(context.data, button)
    }

    private fun buildScrollIndicator(
        context: PanelContext<ListKey, Context>,
        top: Float,
        symbol: String,
        pageDelta: Int,
    ): CustomPanelAPI {
        val buttonId = "${context.key}:$symbol"
        val (shell, button) = CampaignTagScrollButtons.addTagScrollIndicatorMomentaryButton(
            parent = context.panel,
            top = top,
            symbol = symbol,
            data = buttonId,
            height = TAG_ELLIPSIS_HEIGHT,
        ) {
            val region = scrollRegions.firstOrNull { it.key == context.key }
            if (region != null) {
                val current = scrollOffsets[context.key] ?: 0
                val updated = usefulVerticalScrollOffsetByDelta(current, pageDelta, region.maxOffset)
                if (updated != current) {
                    scrollOffsets[context.key] = updated
                    refresh(context.key)
                }
            }
        }
        listOwnedButtonsByKey.getOrPut(context.key) { mutableListOf() }.add(button)
        addScrollIndicatorButton(context.data, button)
        return shell.panel
    }

    private fun removeListOwnedButtons(context: PanelContext<ListKey, Context>) {
        val buttons = listOwnedButtonsByKey.remove(context.key) ?: return
        buttons.forEach { removeScrollIndicatorButton(context.data, it) }
    }

    private fun clearRenderedState(context: PanelContext<ListKey, Context>) {
        val panel = context.panel
        context.children.forEach { child ->
            runCatching { panel.removeComponent(child) }
                .onFailure { ex ->
                    Global.getLogger(PinnedTagListRenderer::class.java)
                        .warn("[AGC_TAG_LIST] Failed to remove stale tag-list child panel", ex)
                }
        }
        context.children.clear()
        scrollRegions.removeAll { it.key == context.key }
        removeListOwnedButtons(context)
        removeButtons(context.data)
    }

    private fun mutableExpandedCategoryTitles(
        initial: Map<ListKey, Set<String>>,
    ): MutableMap<ListKey, MutableSet<String>> {
        val result = mutableMapOf<ListKey, MutableSet<String>>()
        initial.forEach { (key, titles) ->
            result[key] = titles.toMutableSet()
        }
        return result
    }
}
