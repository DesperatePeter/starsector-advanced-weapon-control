package com.dp.advancedgunnerycontrol.combatgui.buttons

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

/**
 * data class describing data required to render button
 *
 * Use GuiBase.addButton so that you don't have to use this directly
 */
data class ButtonInfo(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, var txt: String, val font: LazyFont?, val color: Color,
    val tooltip: HoverTooltip
)
