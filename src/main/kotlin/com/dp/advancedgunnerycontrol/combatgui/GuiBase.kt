package com.dp.advancedgunnerycontrol.combatgui

import com.dp.advancedgunnerycontrol.combatgui.buttongroups.*
import com.dp.advancedgunnerycontrol.combatgui.buttons.ActionButton
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonAction
import com.dp.advancedgunnerycontrol.combatgui.buttons.ButtonInfo
import com.dp.advancedgunnerycontrol.combatgui.buttons.HoverTooltip
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.ui.FontException
import org.lazywizard.lazylib.ui.LazyFont

/**
 * The base class you need to extend/inherit from to create a GUI
 *
 * call the constructor of this class in your constructor (via super) and pass it a guiLayout object
 * you can use the defaultGuiLayout by passing nothing if you want to get started quickly
 *
 * Override [getTitleString] to set a display title
 *
 * call [GuiBase.addButton] and/or [addButtonGroup] in your constructor to define what the GUI does
 *
 * Call this classes [advance] and [render] in a BaseEveryFrame(Combat)Script advance/render methods
 *
 * It makes sense to create a new GUI object when a hotkey is pressed
 *
 * To get started quickly, you can use the [SampleGuiLauncher]
 */
open class GuiBase(private val guiLayout: GuiLayout = defaultGuiLayout) {
    private val gSettings = Global.getSettings()

    private val xSpacing = guiLayout.buttonWidthPx + guiLayout.paddingPx
    private val ySpacing = guiLayout.buttonHeightPx + guiLayout.paddingPx + guiLayout.textSpacingBufferPx
    private val xTooltip = guiLayout.xTooltipRel * gSettings.screenWidthPixels / gSettings.screenScaleMult
    private val yTooltip = guiLayout.yTooltipRel * gSettings.screenHeightPixels / gSettings.screenScaleMult
    private val xAnchor = guiLayout.xAnchorRel * gSettings.screenWidthPixels / gSettings.screenScaleMult
    private val yAnchor = guiLayout.yAnchorRel * gSettings.screenHeightPixels / gSettings.screenScaleMult
    private val xMessage = guiLayout.xMessageRel * gSettings.screenWidthPixels / gSettings.screenScaleMult
    private val yMessage = guiLayout.yMessageRel * gSettings.screenHeightPixels / gSettings.screenScaleMult
    val color = guiLayout.color

    protected var font: LazyFont? = null

    protected val standaloneButtons = mutableListOf<ActionButton>()
    protected val buttonGroups = mutableListOf<DataButtonGroup>()

    /**
     * override this returning a string representing your GUI title
     * may change between frames
     */
    protected open fun getTitleString(): String? {
        return ""
    }

    /**
     * override this to display a message, feel free to return null
     * may change between frames
     */
    protected open fun getMessageString(): String? {
        return ""
    }

    /**
     * This is the intended way of adding button groups
     * adds a new button group to the GUI. This library will take care of positioning based on grid layout.
     * all actions will be automatically executed when appropriate
     *
     * Think of a button group as a row of buttons that can be clicked. Whenever a button gets clicked by the user,
     * it gets activated (i.e. visually highlighted). The user can click the button again to de-activate it.
     * Each button has data (e.g. a string, maybe its name?) associated to it. Whenever a button in this group
     * is clicked by the user, the action of the button group gets executed on the data of all active buttons.
     *
     * @param action will be performed when one of the buttons gets clicked, can't pass null
     *               Implement a class that implements ButtonGroupAction, overriding the execute method
     *               A list of data corresponding to the data of all currently active buttons will be passed to this action
     * @param create will be performed when the button group gets added, create individual buttons in this action, can't pass null
     *               Use the pre-existing class CreateSimpleButtons if you don't want to do anything fancy
     * @param refresh will be called whenever something changes (e.g. any button gets clicked), feel free to pass null
     * @note Internally, this will create a new object that inherits from DataButtonGroup and implements the abstract functions.
     *       If you want to provide your own implementation for DataButtonGroup, use addCustomButtonGroup instead
     */
    protected fun addButtonGroup(
        action: ButtonGroupAction,
        create: CreateButtonsAction,
        refresh: RefreshButtonsAction?,
        descriptionText: String
    ) {
        val group = object : DataButtonGroup(font, descriptionText, createButtonGroupLayout(buttonGroups.size)) {
            override fun createButtons() {
                create.createButtons(this)
            }

            override fun refresh() {
                refresh?.refreshButtons(this)
            }

            override fun executeAction(data: List<Any>, triggeringButtonData: Any?, deselectedButtonData: Any?) {
                action.execute(data, triggeringButtonData, deselectedButtonData)
            }

            override fun onHover() {
                action.onHover()
            }
        }
        group.createButtons()
        buttonGroups.add(group)
    }

    /**
     * It is recommended to use addButtonGroup instead.
     * add a custom button group where you have to take care of positioning etc.
     * You will need to create a new class that inherits from DataButton group and pass an instance to this method
     * actions will be automatically executed when appropriate
     */
    protected fun addCustomButtonGroup(buttonGroup: DataButtonGroup) {
        buttonGroup.createButtons()
        buttonGroups.add(buttonGroup)
    }

    /**
     * add a new button to the GUI and let this library handle positioning
     * a button in this context is the simplest GUI element. If a user clicks it, the passed action gets executed
     * @param action will be executed when the button is click, feel free to pass null
     * @param txt display text AKA name of the button
     * @param tooltipTxt will be displayed when user hovers over button, feel free to pass an empty string
     */
    protected fun addButton(action: ButtonAction?, txt: String, tooltipTxt: String, isDisabled: Boolean = false) {
        val btnInfo = createButtonInfo(standaloneButtons.size, txt, tooltipTxt)
        val btn = ActionButton(action, btnInfo)
        btn.isDisabled = isDisabled
        standaloneButtons.add(btn)
    }

    /**
     * It is recommended to use addButton instead of this
     * add a custom button where you have to take care of positioning etc.
     */
    protected fun addCustomButton(button: ActionButton) {
        standaloneButtons.add(button)
    }

    /**
     * returns layout that would be assigned to button group when using addButtonGroup
     *
     * @note Only relevant if you plan on using addCustomButtonGroup
     */
    protected fun createButtonGroupLayout(index: Int): ButtonGroupLayout {
        return ButtonGroupLayout(
            xAnchor, yAnchor - index * ySpacing, guiLayout.buttonWidthPx, guiLayout.buttonHeightPx,
            guiLayout.a, guiLayout.color, guiLayout.paddingPx, xTooltip, yTooltip
        )
    }

    /**
     * @returns button info that would be assigned to button when using addButton
     *
     * @note Only relevant if you plan on using addCustomButton
     */
    protected fun createButtonInfo(xIndex: Int, txt: String, tooltipTxt: String): ButtonInfo {
        return ButtonInfo(
            xAnchor + xIndex * xSpacing, yAnchor + ySpacing,
            guiLayout.buttonWidthPx, guiLayout.buttonHeightPx, guiLayout.a, txt, font, color, HoverTooltip(
                xTooltip, yTooltip, tooltipTxt
            )
        )
    }

    init {
        try {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
        }
    }

    /**
     * calls the refresh method of every button group
     * gets automatically called in [advance], feel free to call once at the end of your constructor call
     */
    protected open fun refreshButtons() {
        buttonGroups.forEach {
            it.refresh()
        }
    }

    /**
     * call this every frame in your e.g. BaseEveryFrameCombatPlugin
     * executes button logic, such as checking which button was clicked and executing actions when appropriate
     */
    open fun advance() {
        var wasAction = false
        buttonGroups.forEach { wasAction = it.advance() || wasAction }
        standaloneButtons.forEach { wasAction = it.advance() || wasAction }
        if (wasAction) {
            refreshButtons()
        }
    }

    /**
     * delete all buttons from button groups and re-create them with the given CreateButtonsAction
     */
    open fun reRenderButtonGroups() {
        buttonGroups.forEach {
            it.buttons.clear()
            it.resetGrid()
            it.createButtons()
            it.refresh()
        }
    }

    /**
     * call this every frame in your e.g. BaseEveryFrameCombatPlugin
     * renders buttons, texts and tooltips
     */
    open fun render() {
        buttonGroups.forEach { it.render() }
        standaloneButtons.forEach { it.render() }
        getTitleString()?.let { font?.createText(it, color) }?.draw(xAnchor, yAnchor + (2 * ySpacing))
        getMessageString()?.let { font?.createText(it, color) }?.draw(xMessage, yMessage)
    }
}