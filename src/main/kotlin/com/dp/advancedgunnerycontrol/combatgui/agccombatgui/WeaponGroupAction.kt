package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.gui.refitscreen.RefitScreenHandler
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.applyTagsToWeaponGroup
import com.dp.advancedgunnerycontrol.utils.loadTags
import com.dp.advancedgunnerycontrol.utils.saveTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.buttongroups.MagicCombatButtonGroupAction
import org.magiclib.combatgui.MagicCombatRenderShapes.Highlight

class WeaponGroupAction(
    private val ship: ShipAPI,
    private val index: Int,
    private val highlights: MutableList<Highlight>,
    private val viewMult: Float,
    private val campaignMode: Boolean
) : MagicCombatButtonGroupAction {
    override fun execute(data: List<Any>, selectedButtonData: Any?, deselectedButtonData: Any?) {
        val currentTags = loadTags(ship, index, Values.storageIndex)
        var tagStrings = (currentTags + data.filterIsInstance<String>()).toSet().toList()
        (deselectedButtonData as? String)?.let { d ->
            tagStrings = tagStrings.filter { d != it }
        }

        applyTagsToWeaponGroup(ship, index, tagStrings)
        saveTags(ship, index, Values.storageIndex, tagStrings)
    }

    override fun onHover() {
        val weapons = ship.weaponGroupsCopy?.getOrNull(index)?.weaponsCopy ?: return
        val vp = Global.getCombatEngine()?.viewport ?: return

        val toScreenX = { x: Float ->
            if(campaignMode) x + RefitScreenHandler.refitPanelAnchorX else vp.convertWorldXtoScreenX(x)
        }
        val toScreenY = { y: Float ->
            if(campaignMode) y + RefitScreenHandler.refitPanelAnchorY else vp.convertWorldYtoScreenY(y)
        }
        highlights.clear()
        highlights.addAll(
            weapons.map {
                Highlight(toScreenX(it.location.x), toScreenY(it.location.y), it.sprite?.height ?: 10f, 0.25f)
            })
    }


}