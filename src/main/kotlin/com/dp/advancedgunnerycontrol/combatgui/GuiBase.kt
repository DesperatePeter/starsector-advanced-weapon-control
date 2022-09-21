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
    private val xSpacing = guiLayout.buttonWidthPx + guiLayout.paddingPx
    private val ySpacing = guiLayout.buttonHeightPx + guiLayout.paddingPx + guiLayout.textSpacingBufferPx
    private val xTooltip = guiLayout.xTooltipRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    private val yTooltip = guiLayout.yTooltipRel * Global.getSettings().screenHeightPixels / Global.getSettings().screenScaleMult
    private val xAnchor = guiLayout.xAnchorRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    private val yAnchor = guiLayout.yAnchorRel * Global.getSettings().screenHeightPixels / Global.getSettings().screenScaleMult
    private val xMessage = guiLayout.xMessageRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    private val yMessage = guiLayout.yMessageRel * Global.getSettings().screenWidthPixels / Global.getSettings().screenScaleMult
    val color = guiLayout.color

    protected var font: LazyFont? = null

    protected val standaloneButtons = mutableListOf<ActionButton>()
    protected val buttonGroups = mutableListOf<DataButtonGroup>()

    /**
     * override this returning a string representing your GUI title
     */
    protected open fun getTitleString() : String?{
        return ""
    }

    /**
     * override this to display a message, feel free to return null
     */
    protected open fun getMessageString() : String?{
        return ""
    }

    /**
     * adds a new button group to the GUI. This library will take care of positioning based on grid layout.
     * all actions will be automatically executed when appropriate
     * @param action will be performed when one of the buttons gets clicked, can't pass null
     * @param create will be performed when the button group gets added, create individual buttons in this action, can't pass null
     * @param refresh will be called every frame, feel free to pass null
     * @note ButtonGroups represent a set of data and the data of all active buttons will be passed to the action
     */
    protected fun addButtonGroup(action: ButtonGroupAction, create: CreateButtonsAction, refresh: RefreshButtonsAction?, descriptionText: String){
        val group = object : DataButtonGroup(font, descriptionText, createButtonGroupLayout(buttonGroups.size)){
            override fun createButtons() {
                create.createButtons(this)
            }

            override fun refresh() {
                refresh?.refreshButtons(this)
            }

            override fun executeAction(data: List<Any>, triggeringButtonData: Any?) {
                action.execute(data, triggeringButtonData)
           }
        }
        group.createButtons()
        buttonGroups.add(group)
    }

    /**
     * add a custom button group where you have to take care of positioning
     * actions will be automatically executed when appropriate
     */
    protected fun addCustomButtonGroup(buttonGroup: DataButtonGroup){
        buttonGroup.createButtons()
        buttonGroups.add(buttonGroup)
    }

    /**
     * add a new button to the GUI and let this library handle positioning
     * @param action will be executed when the button is click, feel free to pass null
     * @param txt display text
     * @param tooltipTxt will be displayed when user hovers over button
     */
    protected fun addButton(action: ButtonAction?, txt: String, tooltipTxt: String, isDisabled: Boolean = false){
        val btnInfo = createButtonInfo(standaloneButtons.size, txt, tooltipTxt)
        val btn = ActionButton(action, btnInfo)
        btn.isDisabled = isDisabled
        standaloneButtons.add(btn)
    }

    /**
     * add a custom button where you have to take care of positioning
     */
    protected fun addCustomButton(button: ActionButton){
        standaloneButtons.add(button)
    }

    /**
     * returns layout that would be assigned to button group when using addButtonGroup
     *
     * Note: Only relevant if you plan on using addCustomButtonGroup
     */
    protected fun createButtonGroupLayout(index: Int) : ButtonGroupLayout{
        return ButtonGroupLayout(xAnchor, yAnchor - index * ySpacing, guiLayout.buttonWidthPx, guiLayout.buttonHeightPx,
        guiLayout.a, guiLayout.color, guiLayout.paddingPx, xTooltip, yTooltip)
    }

    /**
     * returns button info that would be assigned to button when using addButton
     *
     * Note: Only relevant if you plan on using addCustomButton
     */
    protected fun createButtonInfo(xIndex: Int, txt: String, tooltipTxt: String) : ButtonInfo{
        return ButtonInfo(
            xAnchor + xIndex * xSpacing, yAnchor + ySpacing,
            guiLayout.buttonWidthPx, guiLayout.buttonHeightPx, guiLayout.a, txt, font, color, HoverTooltip(
                xTooltip, yTooltip, tooltipTxt))
    }

    init {
        try {
            font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt")
        } catch (e: FontException) {
            Global.getLogger(this.javaClass).error("Failed to load font, won't de displaying messages", e)
        }
    }

    /**
     * calls the refresh method of every button (group)
     * gets automatically called in [advance], feel free to call once at the end of your constructor call
     */
    protected open fun refreshButtons(){
        buttonGroups.forEach{
            it.refresh()
        }
    }

    /**
     * call this every frame in your e.g. BaseEveryFrameCombatPlugin
     * executes button logic
     */
    open fun advance(){
        buttonGroups.forEach { it.advance() }
        standaloneButtons.forEach { it.advance() }
        refreshButtons()
    }

    /**
     * delete all buttons from button groups and re-create them with the given CreateButtonsAction
     */
    open fun reRenderButtonGroups(){
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
    open fun render(){
        buttonGroups.forEach { it.render() }
        standaloneButtons.forEach { it.render() }
        getTitleString()?.let { font?.createText(it, color) }?.draw(xAnchor, yAnchor + (2 * ySpacing))
        getMessageString()?.let { font?.createText(it, color) }?.draw(xMessage, yMessage)
    }
}