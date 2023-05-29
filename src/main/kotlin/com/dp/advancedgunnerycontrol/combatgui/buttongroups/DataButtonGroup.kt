package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.DataToggleButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import org.lazywizard.lazylib.ui.LazyFont

/**
 * If possible, use GuiBase.addButtonGroup rather than using this class!
 *
 * This class provides an inheritance-based option to create your buttons, whereas addButtonGroup instead allows
 * you to pass actions for creating/refreshing buttons and the action to execute.
 *
 * base class defining a group of buttons with each button representing a possible date and the whole group
 * representing a data set defined by the sum of data of all active buttons.
 *
 * In other words, this represents a row (or column) of buttons. All buttons in that row perform the same action
 * when clicked. When a button is clicked, that action is performed with the data of all active buttons.
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
    fun addButton(text: String, data: Any, tooltip: String, isActive: Boolean = true) {
        val info = ButtonInfo(
            currentX,
            currentY,
            layout.w,
            layout.h,
            layout.a,
            text,
            font,
            layout.color,
            HoverTooltip(layout.xTooltip, layout.yTooltip, tooltip)
        )
        buttons.add(DataToggleButton(data, info))
        buttons.last().isActive = isActive
        if (layout.horizontal) {
            currentX += layout.w + layout.padding
        } else {
            currentY -= layout.h + layout.padding
        }
    }

    fun resetGrid() {
        currentX = layout.x
        currentY = layout.y
    }

    fun disableButton(title: String) {
        buttons.find { it.info.txt == title }?.let { it.isDisabled = true }
    }

    fun refreshAllButtons(data: List<Any>) {
        buttons.forEach {
            it.isActive = data.contains(it.data)
        }
    }

    fun enableAllButtons() {
        buttons.forEach { it.isDisabled = false }
    }

    fun getActiveButtonData(): List<Any> {
        return buttons.mapNotNull { it.getDataIfActive() }
    }

    fun advance(): Boolean {
        if(ButtonBase.enableButtonHoverEffects && buttons.any { it.isHover() }){
            onHover()
        }
        buttons.filter { it.advance() }.let {
            if (it.isNotEmpty()) {
                executeAction(getActiveButtonData(), it.firstOrNull()?.getDataIfActive())
                return true
            }
        }
        return false
    }

    fun render() {
        buttons.forEach { it.render() }
        font?.createText(descriptionText, baseColor = layout.color)?.draw(layout.x, layout.y + descriptionOffset)
    }

    /**
     * Override me!
     * gets called on construction. Create all buttons belonging to this group in the implementation of this method.
     * cf. CreateSimpleButtons for an example implementation.
     */
    abstract fun createButtons()

    /**
     * Override me!
     * gets called whenever a button of any group gets pressed (or something calls for a re-render)
     * If you e.g. want to enable/disable buttons or change tooltips based on the current state, implement
     * that logic in this method. Otherwise, an empty method will do.
     */
    abstract fun refresh()

    /**
     * Override me!
     * gets called whenever a button in this group gets clicked. Implement the actual logic you want your button group
     * to perform in here.
     * @param data a list of the data of all currently active buttons
     * @param triggeringButtonData data of the button that was clicked. Might be null (usually shouldn't).
     * @note Check if data contains triggeringButtonData to see if button was activated or deactivated
     */
    abstract fun executeAction(data: List<Any>, triggeringButtonData: Any? = null)

    /**
     * Override this method to perform some action when the user hovers over a button in the group.
     * @note use getActiveButtonData() if you need to know the current button states in this method.
     */
    open fun onHover(){}
}