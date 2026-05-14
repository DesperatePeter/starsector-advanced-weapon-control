package com.dp.advancedgunnerycontrol.gui.suggestedtags.actions

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignTooltipCopy
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleHeading
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView

internal object SuggestedTagFilterActions {
    fun buildSuggestedTagFilterActions(
        weaponListView: WeaponListView,
        collapsedFilterCategories: MutableSet<WeaponFilter.FilterCategory>,
        onToggleFilter: (WeaponFilter) -> Unit,
    ): List<SuggestedTagUiAction> {
        val actions = mutableListOf<SuggestedTagUiAction>()
        weaponListView.activeFilters().forEach { filter ->
            actions.add(
                SuggestedTagUiAction(
                    filter.name(),
                    tooltip = CampaignTooltipCopy.filterToggle(active = true),
                    active = true,
                    kind = CampaignActionButtonKind.SUGGESTED_FILTER_ACTIVE,
                    activeKind = CampaignActionButtonKind.SUGGESTED_FILTER_ACTIVE
                ) {
                    onToggleFilter(filter)
                }
            )
        }
        WeaponFilter.filterCategories.forEach { category ->
            val categoryFilters = WeaponFilter.filtersIn(category)
            val expanded = !collapsedFilterCategories.contains(category)
            val heading = CampaignToggleHeading(
                title = category.title,
                expanded = expanded,
                active = false,
                subject = "filters"
            )
            actions.add(
                SuggestedTagUiAction(
                    heading.label,
                    tooltip = heading.tooltip,
                    active = false,
                    kind = heading.kind,
                    activeKind = heading.kind
                ) {
                    if (expanded) {
                        collapsedFilterCategories.add(category)
                    } else {
                        collapsedFilterCategories.remove(category)
                    }
                }
            )
            if (expanded) {
                categoryFilters.forEach { filter ->
                    val filterActive = weaponListView.containsFilter(filter)
                    if (filterActive) return@forEach
                    actions.add(
                        SuggestedTagUiAction(
                            filter.name(),
                            tooltip = CampaignTooltipCopy.filterToggle(filterActive),
                            active = filterActive,
                            indent = CampaignGuiStyle.CHILD_ROW_INDENT
                        ) {
                            onToggleFilter(filter)
                        }
                    )
                }
            }
        }
        return actions
    }
}
