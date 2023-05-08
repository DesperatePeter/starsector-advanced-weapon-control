package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings

fun generateShipActions(attributes: GUIAttributes): List<GUIAction> {
    if(Settings.isAdvancedMode) return listOf(
        BackAction(attributes),
        CycleLoadoutAction(attributes),
        CopyLoadoutAction(attributes),
        NextShipAction(attributes),
        ResetAction(attributes),
        ApplySuggestedModeAction(attributes),
        CopyToSameVariantAction(attributes),
        ReloadSettingsAction(attributes),
        SimpleAdvancedAction(attributes),
        GoToSuggestedTagsAction(attributes)
    )
    return listOf(
        BackAction(attributes),
        NextShipAction(attributes),
        SimpleAdvancedAction(attributes)
    )
}