package com.dp.advancedgunnerycontrol.gui.suggestedtags.actions

import com.dp.advancedgunnerycontrol.customlists.WeaponTagListMode
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationModalRequest
import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView

fun suggestedPageNavigationActions(weaponListView: WeaponListView): List<SuggestedTagUiAction> =
    SuggestedTagActionRows.suggestedPageNavigationActions(weaponListView)

fun suggestedTagListModeAction(
    mode: WeaponTagListMode,
    modeCount: Int,
    modeIndex: Int,
    onCycle: (Int) -> Unit,
): SuggestedTagUiAction =
    SuggestedTagActionRows.suggestedTagListModeAction(mode, modeCount, modeIndex, onCycle)

fun suggestedDangerousActions(
    armAction: (SuggestedTagDangerousAction) -> Unit,
): List<SuggestedTagUiAction> =
    SuggestedTagActionRows.suggestedDangerousActions(armAction)

fun suggestedAutoApplyAction(): SuggestedTagUiAction =
    SuggestedTagActionRows.suggestedAutoApplyAction()

fun buildSuggestedTagActionRows(
    weaponListView: WeaponListView,
    suggestedTagListMode: WeaponTagListMode,
    suggestedTagListModeCount: Int,
    suggestedTagListModeIndex: Int,
    cycleSuggestedTagListMode: (Int) -> Unit,
    clearFilters: () -> Unit,
    armDangerousAction: (SuggestedTagDangerousAction) -> Unit,
): List<SuggestedTagUiAction> =
    SuggestedTagActionRows.buildSuggestedTagActionRows(
        weaponListView = weaponListView,
        suggestedTagListMode = suggestedTagListMode,
        suggestedTagListModeCount = suggestedTagListModeCount,
        suggestedTagListModeIndex = suggestedTagListModeIndex,
        cycleSuggestedTagListMode = cycleSuggestedTagListMode,
        clearFilters = clearFilters,
        armDangerousAction = armDangerousAction,
    )

fun suggestedDangerousActionConfirmationRequest(
    pending: SuggestedTagDangerousAction,
    onClear: () -> Unit,
): CampaignConfirmationModalRequest =
    SuggestedTagDangerousActionRequests.suggestedDangerousActionConfirmationRequest(pending, onClear)

fun buildSuggestedTagFilterActions(
    weaponListView: WeaponListView,
    collapsedFilterCategories: MutableSet<WeaponFilter.FilterCategory>,
    onToggleFilter: (WeaponFilter) -> Unit,
): List<SuggestedTagUiAction> =
    SuggestedTagFilterActions.buildSuggestedTagFilterActions(
        weaponListView = weaponListView,
        collapsedFilterCategories = collapsedFilterCategories,
        onToggleFilter = onToggleFilter,
    )
