package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.gui.session.ShipEditorCapabilities
import com.dp.advancedgunnerycontrol.gui.style.CampaignActionButtonKind
import com.dp.advancedgunnerycontrol.settings.Settings

fun generateShipActions(
    attributes: GUIAttributes,
    capabilities: ShipEditorCapabilities = ShipEditorCapabilities.CAMPAIGN,
    advancedMode: Boolean = Settings.isAdvancedMode,
): List<GUIAction> {
    val actions = mutableListOf<GUIAction>()
    if (capabilities.canSelectOtherShips) {
        actions.add(NextShipAction(attributes))
    }
    if (advancedMode) {
        if (capabilities.canCycleLoadout) {
            actions.add(CycleLoadoutAction(attributes))
        }
        actions.add(CycleTagListModeAction(attributes))
        if (capabilities.canCustomizeSuggestedTags) {
            actions.add(GoToSuggestedTagsAction(attributes))
        }
    }
    actions.add(ToggleSimpleAdvancedAction(attributes))
    if (advancedMode) {
        OpenCustomTagManagerAction(attributes).takeIf { it.isVisible() }?.let(actions::add)
        ResetCustomTagListAction(attributes).takeIf { it.isVisible() }?.let(actions::add)
        actions.add(ResetAction(attributes))
        if (capabilities.canReloadSettings) {
            actions.add(ReloadSettingsAction(attributes))
        }
        OpenDebugMenuAction(attributes).takeIf { it.isVisible() }?.let(actions::add)
    }
    return actions
}

fun shipActionButtonKind(action: GUIAction): CampaignActionButtonKind {
    return when (action) {
        is OpenCustomTagManagerAction -> CampaignActionButtonKind.SAVE
        is ResetAction,
        is ReloadSettingsAction,
        is ResetCustomTagListAction -> CampaignActionButtonKind.LOAD
        is OpenDebugMenuAction -> CampaignActionButtonKind.UNCOLOURED
        is CycleTagListModeAction -> CampaignActionButtonKind.UNCOLOURED
        else -> CampaignActionButtonKind.UNCOLOURED
    }
}

fun shipActionRequiresConfirmation(action: GUIAction): Boolean {
    return action.requiresConfirmation()
}
