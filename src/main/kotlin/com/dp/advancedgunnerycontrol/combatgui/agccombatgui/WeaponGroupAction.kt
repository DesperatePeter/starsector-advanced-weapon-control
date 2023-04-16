package com.dp.advancedgunnerycontrol.combatgui.agccombatgui

import com.dp.advancedgunnerycontrol.combatgui.Highlight
import com.dp.advancedgunnerycontrol.combatgui.buttongroups.ButtonGroupAction
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.dp.advancedgunnerycontrol.utils.applyTagsToWeaponGroup
import com.dp.advancedgunnerycontrol.utils.saveTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI

class WeaponGroupAction(
    private val ship: ShipAPI,
    private val index: Int,
    private val highlights: MutableList<Highlight>,
    private val viewMult: Float
) : ButtonGroupAction {
    override fun execute(data: List<Any>, triggeringButtonData: Any?) {
        val tagStrings = data.filterIsInstance<String>()
        applyTagsToWeaponGroup(ship, index, tagStrings)
        saveTags(ship, index, Values.storageIndex, tagStrings)
    }

    override fun onHover() {
        val weapons = ship.weaponGroupsCopy[index].weaponsCopy
        val vp = Global.getCombatEngine()?.viewport ?: return

        val toScreenX = { x: Float ->
            vp.convertWorldXtoScreenX(x)
        }
        val toScreenY = { y: Float ->
            vp.convertWorldYtoScreenY(y)
        }
        highlights.clear()
        highlights.addAll(
            weapons.map {
                Highlight(toScreenX(it.location.x), toScreenY(it.location.y), it.sprite?.height ?: 10f, 0.25f)
            })
    }


}