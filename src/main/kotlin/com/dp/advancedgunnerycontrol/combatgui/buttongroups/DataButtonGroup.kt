package com.dp.advancedgunnerycontrol.combatgui.buttongroups

import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.DataToggleButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import org.lazywizard.lazylib.ui.LazyFont

/**
 * If possible, use GuiBase.addButtonGroup rather than using this class directly!
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

    /**
     * reset grid positions back to original values
     * call this method if you want to e.g. change something and recreate buttons
     */
    fun resetGrid() {
        currentX = layout.x
        currentY = layout.y
    }

    /**
     * disable (i.e. grey out and make un-clickable) button with given title/text/name
     */
    fun disableButton(title: String) {
        buttons.find { it.info.txt == title }?.let { it.isDisabled = true }
    }

    /**
     * refresh state (active/inactive) of all buttons
     * @param data all buttons where button data is contained in this list will be set to active
     */
    fun refreshAllButtons(data: List<Any>) {
        buttons.forEach {
            it.isActive = data.contains(it.data)
        }
    }

    /**
     * hopefully self-explanatory =)
     */
    fun enableAllButtons() {
        buttons.forEach { it.isDisabled = false }
    }

    /**
     * @return list containing the data of all currently active buttons
     */
    fun getActiveButtonData(): List<Any> {
        return buttons.mapNotNull { it.getDataIfActive() }
    }

    /**
     * needs to be called every frame.
     * checks if a button was clicked/hovered over during that frame
     */
    fun advance(): Boolean {
        if(ButtonBase.enableButtonHoverEffects && buttons.any { it.isHover() }){
            onHover()
        }
        buttons.filter { it.advance() }.let {
            val selectedData = it.firstOrNull()?.getDataIfActive()
            val deselectedData = if(selectedData == null) it.firstOrNull()?.data else null
            if (it.isNotEmpty()) {
                executeAction(getActiveButtonData(), selectedData, deselectedData)
                return true
            }
        }
        return false
    }

    /**
     * needs to be called every frame from a render-method, such as e.g. BaseEveryFrameCombatPlugin::renderInUICoords
     */
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
     * @param triggeringButtonData data of the button that was clicked if it was turned active. Null otherwise.
     * @param deselectedButtonData data of the button that was clicked if it was turned inactive. Null otherwise.
     * @note Check if data contains triggeringButtonData to see if button was activated or deactivated
     */
    abstract fun executeAction(data: List<Any>, triggeringButtonData: Any? = null, deselectedButtonData: Any? = null)

    /**
     * Override this method to perform some action when the user hovers over a button in the group.
     * @note use getActiveButtonData() if you need to know the current button states in this method.
     */
    open fun onHover(){}
}