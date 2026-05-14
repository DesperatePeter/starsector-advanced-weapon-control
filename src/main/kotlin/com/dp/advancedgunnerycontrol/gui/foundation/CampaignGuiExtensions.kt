package com.dp.advancedgunnerycontrol.gui.foundation

import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import com.fs.starfarer.api.ui.UIComponentAPI

fun TooltipMakerAPI.addTooltip(
    to: UIComponentAPI,
    location: TooltipLocation,
    width: Float,
    builder: (TooltipMakerAPI) -> Unit,
) {
    addTooltipTo(object : TooltipCreator {
        override fun isTooltipExpandable(tooltipParam: Any?): Boolean = false

        override fun getTooltipWidth(tooltipParam: Any?): Float = width

        override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
            tooltip?.let(builder)
        }
    }, to, location)
}
