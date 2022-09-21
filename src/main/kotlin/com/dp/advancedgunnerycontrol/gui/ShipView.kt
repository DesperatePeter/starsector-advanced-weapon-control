package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.utils.loadAllTags
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.*
import org.lwjgl.opengl.GL11

class ShipView(private val tagView : TagListView) : CustomUIPanelPlugin {
    private var pos : PositionAPI? = null
    private val buttons : MutableList<ButtonBase<*>> = mutableListOf()

    override fun positionChanged(pos: PositionAPI?) {
        pos?.let {
            this.pos = it
        }
    }

    override fun renderBelow(alpha: Float) {}

    override fun render(alpha: Float) {
        pos?.let { p ->
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glColor4f(0f, 150f / 255f, 90f / 255f, 0.6f * alpha)
            GL11.glBegin(GL11.GL_LINES)
            GL11.glLineWidth(10f)

            GL11.glVertex2f(p.x, p.y + p.height)
            GL11.glVertex2f(p.x + p.width, p.y + p.height)
            GL11.glVertex2f(p.x + p.width, p.y + p.height)
            GL11.glVertex2f(p.x + p.width, p.y)
            GL11.glVertex2f(p.x + p.width, p.y)
            GL11.glVertex2f(p.x, p.y)
            GL11.glVertex2f(p.x, p.y)
            GL11.glVertex2f(p.x, p.y + p.height)

            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    private fun addTagButtonGroup(group: Int, ship: FleetMemberAPI, tooltip: TooltipMakerAPI){
        buttons.addAll(TagButton.createModeButtonGroup(ship, group, tooltip, tagView))
    }

    private fun addShipModeButtonGroup(ship: FleetMemberAPI, panel: CustomPanelAPI, position: UIComponentAPI){
        buttons.addAll(ShipModeButton.createModeButtonGroup(ship, panel, position))
    }

    override fun advance(t: Float) {
        buttons.forEach { it.executeCallbackIfChecked() }
        tagView.advance()
    }

    fun shouldRegenerate(): Boolean{
        return tagView.hasViewChanged()
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {}

    fun showShipModes(attributes: GUIAttributes){
        attributes.customPanel = attributes.visualPanel?.showCustomPanel(1210f, 650f, this)
        attributes.customPanel?.position?.inTMid(20f)
        attributes.ship?.let { sh ->
            Settings.hotAddTags(loadAllTags(sh))
            val imgView = attributes.customPanel?.createUIElement(100f, 100f, false)
            imgView?.addImage(sh.hullSpec.spriteName, 80f, 80f, 5.0f)
            attributes.customPanel?.addUIElement(imgView)?.inTL(1f, 1f)
            val shipModeHeader = attributes.customPanel?.createUIElement(1200f, 50f, false)
            shipModeHeader?.addTitle("Ship AI Modes (${sh.shipName}, ${sh.variant?.fullDesignationWithHullNameForShip}):")
            shipModeHeader?.addPara("Tag Scrollbar: ${tagView.asciiScrollBar()}", 5.0f)
            attributes.customPanel?.addUIElement(shipModeHeader)?.rightOfBottom(imgView, 1f)
            attributes.customPanel?.let {
                if (imgView != null) {
                    addShipModeButtonGroup(sh, it, imgView)
                }
            }
            val elements = mutableListOf<UIComponentAPI>()
            for(i in 0 until sh.variant.weaponGroups.size){
                val element = attributes.customPanel?.createUIElement(162f, 500f, false)
                element?.let {
                    it.addTitle("Group ${i+1}")
                    addTagButtonGroup(i, sh, it)
                    it.addImages(162f, 35f, 1f, 1f, *groupWeaponSpriteNames(sh.variant.weaponGroups[i], sh).toTypedArray())
                    it.addPara(groupAsString(sh.variant.weaponGroups[i], sh), 5.0f)
                    it.addPara("${groupFluxCost(sh.variant.weaponGroups[i], sh)} flux/s", 5.0f)
                    // without this call, we get a "can only anchor to siblings" exception
                    attributes.customPanel?.addComponent(it)
                    val pos = attributes.customPanel?.addUIElement(it)
                    pos?.let { p ->
                        if (elements.isNotEmpty()){
                            p.rightOfTop(elements.last(), 10f)
                        }else{
                            p.belowLeft(imgView, 35f)
                        }
                    }
                    elements.add(it)
                }
            }
        }
    }

}