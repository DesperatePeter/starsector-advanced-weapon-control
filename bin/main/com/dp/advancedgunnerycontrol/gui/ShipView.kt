package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.utils.loadAllTags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI

class ShipView(private val tagView: TagListView) : CustomView() {

    private val buttons: MutableList<ButtonBase<*>> = mutableListOf()

    private fun addTagButtonGroup(group: Int, ship: FleetMemberAPI, tooltip: TooltipMakerAPI) {
        buttons.addAll(TagButton.createModeButtonGroup(ship, group, tooltip, tagView))
    }

    private fun addShipModeButtonGroup(ship: FleetMemberAPI, panel: CustomPanelAPI, position: UIComponentAPI) {
        buttons.addAll(ShipModeButton.createModeButtonGroup(ship, panel, position))
    }

    override fun advance(t: Float) {
        buttons.forEach { it.executeCallbackIfChecked() }
        tagView.advance()
    }

    fun shouldRegenerate(): Boolean {
        return tagView.hasChanged()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {}
    override fun buttonPressed(p0: Any?) {}

    fun showShipModes(attributes: GUIAttributes) {
        attributes.customPanel = attributes.visualPanel?.showCustomPanel(1210f, 650f, this)
        attributes.customPanel?.position?.inTMid(20f)
        attributes.ship?.let { sh ->
            Settings.hotAddTags(loadAllTags(sh))
            val imgView = attributes.customPanel?.createUIElement(1200f, 120f, false)
            imgView?.addTitle("${sh.shipName}, ${sh.variant?.fullDesignationWithHullNameForShip}")
            imgView?.addPara("Tag Scrollbar: ${tagView.asciiScrollBar()}", 5.0f)
            imgView?.addImage(sh.hullSpec.spriteName, 80f, 80f, 5.0f)
            attributes.customPanel?.addUIElement(imgView)?.inTL(1f, 1f)
            var lastElement = imgView

            if(Settings.isAdvancedMode){
                val shipModeCard = attributes.customPanel?.createUIElement(1200f, 10f, false)
                shipModeCard?.addTitle("Ship AI Modes")
                attributes.customPanel?.addUIElement(shipModeCard)?.belowLeft(imgView, 1f)
                attributes.customPanel?.let {
                    if (shipModeCard != null) {
                        addShipModeButtonGroup(sh, it, shipModeCard)
                        lastElement = shipModeCard
                    }
                }
            }
            val elements = mutableListOf<UIComponentAPI>()
            for (i in 0 until sh.variant.weaponGroups.size) {
                val element = attributes.customPanel?.createUIElement(162f, 500f, false)
                element?.let {
                    it.addTitle("Group ${i + 1}")
                    addTagButtonGroup(i, sh, it)
                    it.addImages(
                        162f,
                        35f,
                        1f,
                        1f,
                        *groupWeaponSpriteNames(sh.variant.weaponGroups[i], sh).toTypedArray()
                    )
                    it.addPara(groupAsString(sh.variant.weaponGroups[i], sh), 5.0f)
                    it.addPara("${groupFluxCost(sh.variant.weaponGroups[i], sh)} flux/s", 5.0f)
                    // without this call, we get a "can only anchor to siblings" exception
                    attributes.customPanel?.addComponent(it)
                    val pos = attributes.customPanel?.addUIElement(it)
                    pos?.let { p ->
                        if (elements.isNotEmpty()) {
                            p.rightOfTop(elements.last(), 10f)
                        } else {
                            p.belowLeft(lastElement, 35f)
                        }
                    }
                    elements.add(it)
                }
            }
        }
    }

}