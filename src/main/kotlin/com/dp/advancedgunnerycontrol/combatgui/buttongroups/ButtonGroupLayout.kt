package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import java.awt.Color

/**
 * use for custom button groups
 */
data class ButtonGroupLayout(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, val color: Color,
    val padding: Float,
    val xTooltip: Float, val yTooltip: Float,
    val horizontal: Boolean = true
)
