package com.dp.advancedgunnerycontrol.combatgui.buttons

import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

/**
 * data class describing data required to render button
 *
 * Only use directly to add custom buttons (it's recommended to use GuiBase.addButton instead)
 */
data class ButtonInfo(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, var txt: String, val font: LazyFont?, val color: Color,
    val tooltip: HoverTooltip
)
