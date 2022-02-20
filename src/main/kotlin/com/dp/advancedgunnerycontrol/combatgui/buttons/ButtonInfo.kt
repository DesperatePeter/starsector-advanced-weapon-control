package com.dp.advancedgunnerycontrol.combatgui.buttons

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

data class ButtonInfo(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, val txt: String, val font: LazyFont?, val color: Color,
    val tooltip: HoverTooltip
)
