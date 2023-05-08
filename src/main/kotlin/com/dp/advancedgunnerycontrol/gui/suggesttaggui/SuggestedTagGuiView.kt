package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.dp.advancedgunnerycontrol.gui.CustomView
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.VisualPanelAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI

class SuggestedTagGuiView(private val tagView: TagListView, private val weaponListView: WeaponListView) : CustomView() {
    private val buttons: MutableList<SuggestedTagButton> = mutableListOf()

    private fun addButtonGroup(weaponId: String, tooltipMaker: TooltipMakerAPI){
        buttons.addAll(SuggestedTagButton.createButtonGroup(weaponId, tooltipMaker, tagView))
    }
    override fun advance(p0: Float) {
        buttons.forEach { it.executeCallbackIfChecked() }
        tagView.advance()
    }

    fun show(panel: VisualPanelAPI): CustomPanelAPI{
        val customPanel = panel.showCustomPanel(1410f, 650f, this)
        customPanel?.position?.inTMid(20f)
        val header = customPanel.createUIElement(1400f, 50f, false)
        header?.addTitle("Customize suggested weapon tags.")
        header?.addPara("When you click the button to apply suggested tags, these tags will be used.", 2f)
        header?.addPara("Tag Scrollbar: ${tagView.asciiScrollBar()}", 5.0f)
        header?.addPara(weaponListView.pageString, 5f)
        customPanel?.addUIElement(header)?.inTL(1f, 1f)
        val elements = mutableListOf<UIComponentAPI>()
        weaponListView.currentIds().forEach { weaponId ->
            val el = customPanel?.createUIElement(162f, 500f, false)
            el?.let {
                var name = WeaponListView.nameFromId(weaponId)
                if(it.computeStringWidth(name) < 155f){
                    name += "\n"
                }
                it.addPara(name, 2f)
                addButtonGroup(weaponId, it)
                it.addImage(Global.getSettings().getWeaponSpec(weaponId).turretSpriteName, 1f)
                customPanel.addComponent(it)
                val p = customPanel.addUIElement(it)
                if (elements.isNotEmpty()) {
                    p.rightOfTop(elements.last(), 10f)
                } else {
                    p.belowLeft(header, 35f)
                }
                elements.add(it)
            }
        }
        return customPanel
    }
    override fun processInput(p0: MutableList<InputEventAPI>?) {}

    override fun buttonPressed(p0: Any?) {}
}