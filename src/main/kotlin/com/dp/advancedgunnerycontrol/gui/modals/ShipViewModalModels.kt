package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.CustomPanelAPI

internal data class ShipViewModalBounds(
    val width: Float,
    val height: Float,
    val x: Float,
    val y: Float,
)

internal data class ShipViewModalShell(
    val backdrop: CustomPanelAPI,
    val dialog: CustomPanelAPI,
    val bounds: ShipViewModalBounds,
)
