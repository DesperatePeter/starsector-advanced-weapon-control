package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.DataToggleButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import org.lazywizard.lazylib.ui.LazyFont

/**
 * If possible, use GuiBase.addButtonGroup rather than using this class!
 *
 * base class defining a group of buttons with each button representing a possible date and the whole group
 * representing a data set defined by the sum of data of all active buttons.
 *
 * buttons get activated/deactivated by the user by clicking on them
 * when a button is clicked, executeAction gets called with the sum of data of all active buttons
 *
 * If, for instance, we have two buttons with corresponding data 1 and respectively, and the user
 * activates the first button, `[1]` will be passed as data to the groupAction and 1 as triggeringButtonData.
 * If the user then clicks the second button `[1, 2]`, will be passed as data and 2 as triggeringButtonData.
 *
 * Extend this class by implementing createButtons, refresh and executeAction
 * @param font LazyFont object
 * @param descriptionText text to be rendered above the group
 * @param layout defines where/how the group gets rendered
 */
abstract class DataButtonGroup(
    val font: LazyFont?, var descriptionText: String, val layout: ButtonGroupLayout
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
    fun resetGrid(){
        currentX = layout.x
        currentY = layout.y
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

    /**
     * gets called on construction. Create
     */
    abstract fun createButtons()
    abstract fun refresh()
    abstract fun executeAction(data : List<Any>, triggeringButtonData: Any? = null)
}