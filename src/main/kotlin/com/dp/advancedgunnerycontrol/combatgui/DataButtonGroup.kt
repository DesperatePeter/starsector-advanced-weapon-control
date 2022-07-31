package com.dp.advancedgunnerycontrol.combatgui

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.DataToggleButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class DataButtonGroup(
    val x: Float, val y: Float, val w: Float, val h: Float,
    val a: Float, val font: LazyFont?, val color: Color,
    val padding: Float, val action: ButtonGroupAction,
    val xTooltip: Float, val yTooltip: Float,
    val descriptionText: String, val horizontal: Boolean = true
) {
    val buttons = mutableListOf<DataToggleButton>()
    private val descriptionOffset = 40f
    private var currentX = x
    private var currentY = y
    fun addButton(text: String, data: Any, tooltip: String, isActive : Boolean = true) {
        val info = ButtonInfo(currentX, currentY, w, h, a, text, font, color, HoverTooltip(xTooltip, yTooltip, tooltip))
        buttons.add(DataToggleButton(data, info))
        buttons.last().isActive = isActive
        if(horizontal){
            currentX += w + padding
        }else{
            currentY -= h + padding
        }
    }
    fun disableButton(text: String){
        buttons.find { it.info.txt == text }?.let { it.isDisabled = true }
    }
    fun refreshAllButtons(data : List<Any>){
        buttons.forEach {
            it.isActive = data.contains(it.data)
        }
    }
    fun enableAllButtons(){
        buttons.forEach { it.isDisabled = false }
    }
    fun getData() : List<Any>{
        return buttons.mapNotNull { it.getDataIfActive() }
    }
    fun advance(){
        if(buttons.count { it.advance() } > 0){ // if at least one button was pressed
            action.execute(buttons.mapNotNull { it.getDataIfActive() })
        }
    }
    fun render(){
        buttons.forEach { it.render() }
        font?.createText(descriptionText, baseColor = color)?.draw(x, y + descriptionOffset)
    }
}