package com.dp.advancedgunnerycontrol.gui.suggestedtags.actions

import com.dp.advancedgunnerycontrol.customlists.WeaponTagListMode
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignTooltipCopy
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.gui.suggestedtags.view.WeaponListView
import com.dp.advancedgunnerycontrol.settings.LunaSettingHandler
import com.dp.advancedgunnerycontrol.settings.Settings
import org.lwjgl.input.Keyboard

internal object SuggestedTagActionRows {
    fun suggestedPageNavigationActions(weaponListView: WeaponListView): List<SuggestedTagUiAction> {
        return listOf(
            SuggestedTagUiAction(
                "Next Page",
                listOf(Keyboard.KEY_D),
                CampaignTooltipCopy.NEXT_PAGE,
                onRightClick = { weaponListView.cycleBackwards() }
            ) { weaponListView.cycle() },
            SuggestedTagUiAction(
                "Prev Page",
                listOf(Keyboard.KEY_A),
                CampaignTooltipCopy.PREV_PAGE,
                onRightClick = { weaponListView.cycle() }
            ) { weaponListView.cycleBackwards() }
        )
    }

    fun suggestedTagListModeAction(
        mode: WeaponTagListMode,
        modeCount: Int,
        modeIndex: Int,
        onCycle: (Int) -> Unit,
    ): SuggestedTagUiAction {
        return SuggestedTagUiAction(
            "Tag list: ${mode.displayName} [${modeIndex + 1}/$modeCount]",
            tooltip = CampaignTooltipCopy.suggestedTagListMode(mode),
            onRightClick = { onCycle(-1) }
        ) {
            onCycle(1)
        }
    }

    fun suggestedDangerousActions(
        armAction: (SuggestedTagDangerousAction) -> Unit,
    ): List<SuggestedTagUiAction> {
        return listOf(
            suggestedBackupAction(armAction),
            suggestedResetAction(armAction),
            suggestedRestoreAction(armAction),
        )
    }

    private fun suggestedBackupAction(armAction: (SuggestedTagDangerousAction) -> Unit): SuggestedTagUiAction =
        SuggestedTagUiAction(
            "Backup Suggested Tags",
            tooltip = CampaignTooltipCopy.BACKUP_SUGGESTED_TAGS,
            kind = CampaignActionButtonKind.SAVE,
            rebuildAfter = false
        ) {
            armAction(SuggestedTagDangerousAction.BACKUP)
        }

    private fun suggestedResetAction(armAction: (SuggestedTagDangerousAction) -> Unit): SuggestedTagUiAction =
        SuggestedTagUiAction(
            "Reset Suggested Tags",
            tooltip = CampaignTooltipCopy.RESET_SUGGESTED_TAGS,
            kind = CampaignActionButtonKind.LOAD,
            rebuildAfter = false
        ) {
            armAction(SuggestedTagDangerousAction.RESET)
        }

    private fun suggestedRestoreAction(armAction: (SuggestedTagDangerousAction) -> Unit): SuggestedTagUiAction =
        SuggestedTagUiAction(
            "Restore Suggested Tags",
            tooltip = CampaignTooltipCopy.RESTORE_SUGGESTED_TAGS,
            kind = CampaignActionButtonKind.LOAD,
            rebuildAfter = false
        ) {
            armAction(SuggestedTagDangerousAction.RESTORE)
        }

    fun suggestedAutoApplyAction(): SuggestedTagUiAction {
        return if (Settings.autoApplySuggestedTags) {
            SuggestedTagUiAction("Disable Auto-apply", tooltip = CampaignTooltipCopy.autoApplyToggle(enabled = true)) {
                Settings.autoApplySuggestedTags = false
            }
        } else {
            SuggestedTagUiAction("Enable Auto-apply", tooltip = CampaignTooltipCopy.autoApplyToggle(enabled = false)) {
                Settings.autoApplySuggestedTags = true
            }
        }
    }

    fun buildSuggestedTagActionRows(
        weaponListView: WeaponListView,
        suggestedTagListMode: WeaponTagListMode,
        suggestedTagListModeCount: Int,
        suggestedTagListModeIndex: Int,
        cycleSuggestedTagListMode: (Int) -> Unit,
        clearFilters: () -> Unit,
        armDangerousAction: (SuggestedTagDangerousAction) -> Unit,
    ): List<SuggestedTagUiAction> {
        val actions = mutableListOf<SuggestedTagUiAction>()
        actions.addAll(suggestedPageNavigationActions(weaponListView))
        actions.add(
            suggestedTagListModeAction(
                mode = suggestedTagListMode,
                modeCount = suggestedTagListModeCount,
                modeIndex = suggestedTagListModeIndex,
                onCycle = cycleSuggestedTagListMode,
            )
        )
        actions.add(suggestedBackupAction(armDangerousAction))
        actions.add(
            SuggestedTagUiAction(
                "Reset Filters",
                tooltip = CampaignTooltipCopy.RESET_FILTERS,
                kind = CampaignActionButtonKind.LOAD
            ) {
                clearFilters()
            }
        )
        if (!LunaSettingHandler.isLunaLibPresent) {
            actions.add(suggestedAutoApplyAction())
        }
        actions.add(suggestedResetAction(armDangerousAction))
        actions.add(suggestedRestoreAction(armDangerousAction))
        return actions
    }
}
