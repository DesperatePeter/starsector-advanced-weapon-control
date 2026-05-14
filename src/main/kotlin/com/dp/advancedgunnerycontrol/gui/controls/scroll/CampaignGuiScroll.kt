package com.dp.advancedgunnerycontrol.gui.controls.scroll

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle

import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class VerticalScrollSlice<T>(
    val offset: Int,
    val items: List<T>,
    val hasAbove: Boolean,
    val hasBelow: Boolean,
    val maxOffset: Int,
)

data class VerticalScrollRegion<K>(
    val key: K,
    val left: Float,
    val right: Float,
    val bottom: Float,
    val top: Float,
    val maxOffset: Int,
)

fun rectangularRegionContainsEvent(
    event: InputEventAPI,
    panelPos: PositionAPI?,
    left: Float,
    right: Float,
    bottom: Float,
    top: Float,
): Boolean {
    val resolvedPanelPos = panelPos ?: return false
    val mouseX = event.x.toFloat()
    val mouseY = event.y.toFloat()
    return mouseX in (resolvedPanelPos.x + left)..(resolvedPanelPos.x + right) &&
        mouseY in (resolvedPanelPos.y + bottom)..(resolvedPanelPos.y + top)
}

fun <K> verticalScrollRegionContainsEvent(
    event: InputEventAPI,
    panelPos: PositionAPI?,
    region: VerticalScrollRegion<K>,
): Boolean {
    return rectangularRegionContainsEvent(
        event = event,
        panelPos = panelPos,
        left = region.left,
        right = region.right,
        bottom = region.bottom,
        top = region.top,
    )
}

data class PinnedVerticalScrollLayout<T>(
    val pinnedItems: List<T>,
    val scrollSlice: VerticalScrollSlice<T>,
    val pinnedHeight: Float,
    val pinnedScrollGap: Float,
    val topIndicatorHeight: Float,
    val bottomIndicatorHeight: Float,
    val renderedScrollItemsHeight: Float,
    val visibleScrollAreaHeight: Float,
)

fun usefulVerticalScrollOffset(
    requestedOffset: Int,
    maxOffset: Int,
    scrollDelta: Int = 0,
): Int {
    val clamped = requestedOffset.coerceIn(0, maxOffset.coerceAtLeast(0))
    if (clamped != 1) return clamped
    return if (scrollDelta > 0 && maxOffset >= 2) 2 else 0
}

fun usefulVerticalScrollOffsetByDelta(
    currentOffset: Int,
    scrollDelta: Int,
    maxOffset: Int,
): Int = usefulVerticalScrollOffset(currentOffset + scrollDelta, maxOffset, scrollDelta)

fun computeVerticalSlotCount(
    containerHeight: Float,
    itemHeight: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
    verticalGap: Float = CampaignGuiStyle.TAG_ITEM_VGAP,
): Int {
    val perRow = itemHeight + verticalGap
    return max(1, floor((containerHeight + verticalGap) / perRow).toInt())
}

fun computeVerticalItemsHeight(
    itemCount: Int,
    itemHeight: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
    verticalGap: Float = CampaignGuiStyle.TAG_ITEM_VGAP,
): Float {
    return if (itemCount <= 0) {
        0f
    } else {
        itemCount * itemHeight + max(0, itemCount - 1) * verticalGap
    }
}

fun <T> computeVerticalScrollSlice(
    itemsToRender: List<T>,
    containerHeight: Float,
    currentOffset: Int,
    itemHeight: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
    verticalGap: Float = CampaignGuiStyle.TAG_ITEM_VGAP,
): VerticalScrollSlice<T> {
    val itemCount = itemsToRender.size
    val totalSlots = computeVerticalSlotCount(containerHeight, itemHeight, verticalGap)
    if (itemCount <= totalSlots) {
        return VerticalScrollSlice(0, itemsToRender, false, false, 0)
    }

    val maxOffset = max(0, itemCount - 1)
    var offset = usefulVerticalScrollOffset(currentOffset, maxOffset)
    var hasAbove: Boolean
    var hasBelow: Boolean
    var visibleSlots: Int
    var finalMaxOffset: Int
    while (true) {
        hasAbove = offset > 0
        visibleSlots = totalSlots - if (hasAbove) 1 else 0
        hasBelow = offset + visibleSlots < itemCount
        if (hasBelow) visibleSlots -= 1
        if (visibleSlots <= 0) visibleSlots = 1

        finalMaxOffset = max(0, itemCount - visibleSlots)
        val usefulOffset = usefulVerticalScrollOffset(offset, finalMaxOffset)
        if (usefulOffset == offset) break
        offset = usefulOffset
    }
    val endExclusive = min(itemCount, offset + visibleSlots)
    hasBelow = endExclusive < itemCount
    return VerticalScrollSlice(
        offset = offset,
        items = itemsToRender.subList(offset, endExclusive),
        hasAbove = hasAbove,
        hasBelow = hasBelow,
        maxOffset = finalMaxOffset,
    )
}

fun visibleScrollPageDelta(slice: VerticalScrollSlice<*>): Int {
    return max(1, slice.items.size)
}

fun <T> computePinnedVerticalScrollLayout(
    pinnedCandidates: List<T>,
    scrollCandidates: List<T>,
    containerHeight: Float,
    currentOffset: Int,
    itemHeight: Float = CampaignGuiStyle.TAG_ITEM_HEIGHT,
    verticalGap: Float = CampaignGuiStyle.TAG_ITEM_VGAP,
    indicatorHeight: Float = itemHeight,
): PinnedVerticalScrollLayout<T> {
    val totalSlots = computeVerticalSlotCount(containerHeight, itemHeight, verticalGap)
    val allRowsFit = pinnedCandidates.size + scrollCandidates.size <= totalSlots
    if (allRowsFit) {
        val pinnedHeight = computeVerticalItemsHeight(pinnedCandidates.size, itemHeight, verticalGap)
        val pinnedScrollGap = if (pinnedCandidates.isNotEmpty() && scrollCandidates.isNotEmpty()) verticalGap else 0f
        return PinnedVerticalScrollLayout(
            pinnedItems = pinnedCandidates,
            scrollSlice = VerticalScrollSlice(
                offset = 0,
                items = scrollCandidates,
                hasAbove = false,
                hasBelow = false,
                maxOffset = 0,
            ),
            pinnedHeight = pinnedHeight,
            pinnedScrollGap = pinnedScrollGap,
            topIndicatorHeight = 0f,
            bottomIndicatorHeight = 0f,
            renderedScrollItemsHeight = computeVerticalItemsHeight(scrollCandidates.size, itemHeight, verticalGap),
            visibleScrollAreaHeight = max(itemHeight, containerHeight - pinnedHeight - pinnedScrollGap),
        )
    }
    val pinnedItems = pinnedCandidates.take(max(0, totalSlots - 1))
    val pinnedHeight = computeVerticalItemsHeight(pinnedItems.size, itemHeight, verticalGap)
    val pinnedScrollGap = if (pinnedItems.isNotEmpty() && scrollCandidates.isNotEmpty()) verticalGap else 0f
    val normalAreaHeight = max(itemHeight, containerHeight - pinnedHeight - pinnedScrollGap)
    val scrollSlice = computeVerticalScrollSlice(
        itemsToRender = scrollCandidates,
        containerHeight = normalAreaHeight,
        currentOffset = currentOffset,
        itemHeight = itemHeight,
        verticalGap = verticalGap,
    )
    val topIndicatorHeight = if (scrollSlice.hasAbove) indicatorHeight else 0f
    val bottomIndicatorHeight = if (scrollSlice.hasBelow) indicatorHeight else 0f
    val renderedScrollItemsHeight = computeVerticalItemsHeight(scrollSlice.items.size, itemHeight, verticalGap)
    val visibleScrollAreaHeight = max(itemHeight, normalAreaHeight - topIndicatorHeight - bottomIndicatorHeight)
    return PinnedVerticalScrollLayout(
        pinnedItems = pinnedItems,
        scrollSlice = scrollSlice,
        pinnedHeight = pinnedHeight,
        pinnedScrollGap = pinnedScrollGap,
        topIndicatorHeight = topIndicatorHeight,
        bottomIndicatorHeight = bottomIndicatorHeight,
        renderedScrollItemsHeight = renderedScrollItemsHeight,
        visibleScrollAreaHeight = visibleScrollAreaHeight,
    )
}

fun <K> handleVerticalScrollInput(
    events: MutableList<InputEventAPI>?,
    panelPos: PositionAPI?,
    regions: List<VerticalScrollRegion<K>>,
    currentOffset: (K) -> Int,
    setOffset: (K, Int) -> Unit,
    onScrolled: (K) -> Unit,
    scrollStep: Int = 1,
    normalizeOffset: (K, Int, Int, Int) -> Int = { _, requested, maxOffset, _ ->
        requested.coerceIn(0, maxOffset)
    },
): Boolean {
    if (regions.isEmpty()) return false
    val resolvedPanelPos = panelPos ?: return false
    var scrolled = false
    events?.forEach { event ->
        if (event.isConsumed || !event.isMouseScrollEvent) return@forEach
        val eventValue = event.eventValue
        if (eventValue == 0) return@forEach
        // Use AGC-owned absolute hitboxes instead of raw global mouse polling;
        // direct LWJGL coordinates previously routed wheel input to stale columns.
        val region = regions.firstOrNull { verticalScrollRegionContainsEvent(event, resolvedPanelPos, it) }
            ?: return@forEach
        event.consume()

        if (region.maxOffset <= 0) return@forEach
        val delta = if (eventValue > 0) -scrollStep else scrollStep
        val current = currentOffset(region.key)
        val updated = normalizeOffset(region.key, current + delta, region.maxOffset, delta)
        if (updated != current) {
            setOffset(region.key, updated)
            onScrolled(region.key)
            scrolled = true
        }
    }
    return scrolled
}
