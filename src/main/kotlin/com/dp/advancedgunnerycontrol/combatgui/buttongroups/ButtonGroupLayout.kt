package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

data class ButtonGroupLayout(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, val color: Color,
    val padding: Float,
    val xTooltip: Float, val yTooltip: Float,
    val horizontal: Boolean = true
)
