package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.panels.taglists.PinnedTagListRenderer
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.controls.SuggestedTagButton
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI

/**
 * Suggested-tag list component for one weapon panel.
 * Shares the pinned Active/Inactive tag list behavior with weapon groups while
 * saving selected tags to the suggested-tag preset map for a weapon id.
 */
class SuggestedTagListRenderer(
    initialScrollOffsets: Map<String, Int>,
    initialExpandedCategoryTitles: Map<String, Set<String>> = emptyMap(),
    private val buttons: MutableList<ButtonBase<*>>,
    private val onMissingRefreshContext: () -> Unit = {},
) {
    private data class SuggestedTagListContext(
        val weaponId: String,
        val visibleTags: List<String>,
        val visibleTagSet: Set<String>,
    )

    private val renderer = PinnedTagListRenderer<String, SuggestedTagListContext>(
        initialScrollOffsets = initialScrollOffsets,
        initialExpandedCategoryTitles = initialExpandedCategoryTitles,
        scrollStep = CampaignGuiStyle.WEAPON_TAG_SCROLL_STEP,
        availableTags = { context -> context.visibleTags },
        selectedTags = { context ->
            SuggestedTagButton.supportedSuggestedTagsForWeapon(context.weaponId, context.visibleTagSet)
        },
        removeButtons = { context ->
            buttons.removeAll { it is SuggestedTagButton && it.isForWeapon(context.weaponId) }
        },
        unavailableTags = { _, candidateTags, selectedTags ->
            SuggestedTagButton.unavailableTagsForSelection(candidateTags, selectedTags)
        },
        addTagButtons = { context, panel, tags, pinned, _, selectedTags, unavailableTags, onSelectionChanged ->
            buttons.addAll(
                SuggestedTagButton.createCampaignButtonGroup(
                    context.weaponId,
                    panel,
                    tags,
                    pinned,
                    selectedTagsOverride = selectedTags,
                    unavailableTagsOverride = unavailableTags,
                    onSelectionChanged = { _ -> onSelectionChanged() },
                )
            )
        },
        addScrollIndicatorButton = { _, button -> buttons.add(button) },
        removeScrollIndicatorButton = { _, button -> buttons.remove(button) },
        onMissingRefreshContext = onMissingRefreshContext,
    )

    fun clear() = renderer.clear()

    fun captureScrollOffsets(): Map<String, Int> = renderer.captureScrollOffsets()

    fun captureExpandedCategoryTitles(): Map<String, Set<String>> = renderer.captureExpandedCategoryTitles()

    fun processInput(events: MutableList<InputEventAPI>?, panelPos: PositionAPI?): Boolean =
        renderer.processInput(events, panelPos)

    fun build(
        panel: CustomPanelAPI,
        weaponId: String?,
        relativeLeft: Float,
        relativeBottom: Float,
        visibleTags: List<String>,
    ) {
        if (weaponId == null) return
        renderer.build(
            key = weaponId,
            data = SuggestedTagListContext(
                weaponId = weaponId,
                visibleTags = visibleTags,
                visibleTagSet = visibleTags.toSet(),
            ),
            panel = panel,
            relativeScrollLeft = relativeLeft,
            relativeScrollRight = relativeLeft + panel.position.width,
            relativeScrollBottom = relativeBottom,
            relativeScrollTop = relativeBottom + panel.position.height,
        )
    }
}
