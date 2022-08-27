package com.dp.advancedgunnerycontrol.combatgui

import java.awt.Color

/**
 * @param xAnchorRel position in relative coordinates (value between 0 and 1) where the top-left corner of first
 * button group of the UI shall be placed. Note that ActionButtons will be placed above this.
 * @param yAnchorRel cf. xAnchorRel
 * @param buttonWidthPx button width in pixels, e.g. 100f
 * @param buttonHeightPx button height in pixels, e.g. 20f
 * @param a value between 0 and 1, button opacity, e.g. 0.5f
 * @param color applies to buttons and text
 * @param paddingPx space between buttons in pixels, e.g. 5f
 * @param xTooltipRel position in relative coordinates (value between 0 and 1) where tooltips will be displayed
 * @param yTooltipRel cf. xTooltipRel
 * @param textSpacingBufferPx space in pixels to reserve for text, e.g. 25f
 * @param fontPath path to a Starsector font, e.g. "graphics/fonts/insignia15LTaa.fnt"
 * @param xMessageRel position in relative coordinates (value between 0 and 1) where messages will be displayed
 * @param yMessageRel cf. xMessageRel
 */
data class GuiLayout(
    val xAnchorRel: Float, val yAnchorRel: Float, val buttonWidthPx: Float, val buttonHeightPx: Float,
    val a: Float, val color: Color, val paddingPx: Float, val xTooltipRel: Float, val yTooltipRel: Float,
    val textSpacingBufferPx: Float, val fontPath : String, val xMessageRel: Float, val yMessageRel: Float
)