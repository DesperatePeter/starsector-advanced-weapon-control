package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.GUIAttributes

fun generateShipActions(attributes: GUIAttributes): List<GUIAction> {
    return listOf(
        BackAction(attributes),
        CycleLoadoutAction(attributes),
        CopyLoadoutAction(attributes),
        NextShipAction(attributes),
        ResetAction(attributes),
        ApplySuggestedModeAction(attributes),
        CopyToSameVariantAction(attributes)
    )
}