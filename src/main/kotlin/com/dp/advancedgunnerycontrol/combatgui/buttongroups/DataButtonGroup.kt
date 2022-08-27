package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.DataToggleButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import org.lazywizard.lazylib.ui.LazyFont

abstract class DataButtonGroup(
val font: LazyFont?, val descriptionText: String, val layout: ButtonGroupLayout
) {
    val buttons = mutableListOf<DataToggleButton>()
    private val descriptionOffset = 40f
    private var currentX = layout.x
    private var currentY = layout.y
    fun addButton(text: String, data: Any, tooltip: String, isActive : Boolean = true) {
        val info = ButtonInfo(currentX, currentY, layout.w, layout.h, layout.a, text, font, layout.color, HoverTooltip(layout.xTooltip, layout.yTooltip, tooltip))
        buttons.add(DataToggleButton(data, info))
        buttons.last().isActive = isActive
        if(layout.horizontal){
            currentX += layout.w + layout.padding
        }else{
            currentY -= layout.h + layout.padding
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
    fun getActiveButtonData() : List<Any>{
        return buttons.mapNotNull { it.getDataIfActive() }
    }
    fun advance(){
        buttons.filter { it.advance() }.let {
            if(it.isNotEmpty()){
                executeAction(buttons.mapNotNull { btn -> btn.getDataIfActive() }, it.firstOrNull()?.getDataIfActive())
            }
        }
    }
    fun render(){
        buttons.forEach { it.render() }
        font?.createText(descriptionText, baseColor = layout.color)?.draw(layout.x, layout.y + descriptionOffset)
    }
    abstract fun createButtons()
    abstract fun refresh()
    abstract fun executeAction(data : List<Any>, triggeringButtonData: Any? = null)
}