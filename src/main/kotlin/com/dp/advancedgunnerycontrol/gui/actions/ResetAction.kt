package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.config.Values
import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.shipdata.persistTags
import org.lwjgl.input.Keyboard

class ResetAction(attributes: GUIAttributes) : GUIAction(attributes) {
    fun executeWithModifiers(allLoadouts: Boolean, wholeFleet: Boolean) {
        val loadouts = if (allLoadouts) {
            (0 until Settings.maxLoadouts()).toList()
        } else {
            listOf(AGCGUI.storageIndex)
        }
        val ships = affectedShips(wholeFleet)

        loadouts.forEach { index ->
            ships.forEach { ship ->
                val shipId = agcStableShipId(ship)
                Settings.shipModeStorage.getOrNull(index)?.modesByShip?.get(shipId)?.clear()
                for(i in 0 until Values.MAX_WEAPON_GROUPS){
                    persistTags(shipId, i, index, emptyList())
                }
            }
        }
    }

    override fun execute() {
        executeWithModifiers(
            allLoadouts = isAllLoadoutsKeyHeld(),
            wholeFleet = isWholeFleetKeyHeld()
        )
    }

    override fun getTooltip(): String {
        return "Clear weapon tags and ship modes.\n$modifiersBoilerplateText"
    }

    override fun requiresConfirmation(): Boolean = true

    override fun getConfirmationTitle(): String = "Reset Tags and Ship Modes Warning"

    override fun getConfirmationTone(): CampaignConfirmationTone = CampaignConfirmationTone.WARNING

    override fun getName(): String = "Reset tags and ship modes" + nameSuffix()

    override fun getStableLayoutName(): String = "Reset tags and ship modes"

    override fun getShortcut(): Int = Keyboard.KEY_DELETE

    override fun getDisplayShortcut(): Int? = null
}
