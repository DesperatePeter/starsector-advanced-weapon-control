package com.dp.advancedgunnerycontrol.gui.suggestedtags.state

import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.SuggestedTagGuiView


data class SuggestedTagSessionState(
    val tagScrollOffsets: Map<String, Int> = emptyMap(),
    val tagExpandedCategoryTitles: Map<String, Set<String>> = emptyMap(),
    val collapsedWeaponInfoSections: Set<String> = emptySet(),
    val expandedAdvancedWeaponInfoSections: Set<String> = emptySet(),
    val actionScrollOffset: Int = 0,
    val filterScrollOffset: Int = 0,
) {
    fun captureFrom(view: SuggestedTagGuiView?): SuggestedTagSessionState {
        return if (view == null) {
            this
        } else {
            copy(
                tagScrollOffsets = view.captureTagScrollOffsets(),
                tagExpandedCategoryTitles = view.captureTagExpandedCategoryTitles(),
                collapsedWeaponInfoSections = view.captureCollapsedWeaponInfoSections(),
                expandedAdvancedWeaponInfoSections = view.captureExpandedAdvancedWeaponInfoSections(),
            )
        }
    }

    fun withFilterScrollOffset(offset: Int): SuggestedTagSessionState {
        return copy(filterScrollOffset = offset.coerceAtLeast(0))
    }

    fun withActionScrollOffset(offset: Int): SuggestedTagSessionState {
        return copy(actionScrollOffset = offset.coerceAtLeast(0))
    }
}
