package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.customlists.CustomWeaponTagListStore
import com.dp.advancedgunnerycontrol.gui.session.CustomListModalMode
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

class OpenCustomTagManagerAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        attributes.clearCustomListModalState()
        attributes.customListModalMode = CustomListModalMode.MANAGE_TAGS
    }

    fun isVisible(): Boolean {
        val ship = attributes.ship ?: return false
        val shipId = ShipEditorPersistenceContext(ship, attributes.runtimeShip).shipId.takeIf { it.isNotBlank() }
            ?: return false
        return CustomWeaponTagListStore.getActiveModeOrDefault(shipId).isCustom
    }

    override fun getTooltip(): String {
        return "Open the manager for the current Custom list. Add, edit, or remove visible weapon tags and ship modes, then review and confirm the staged changes."
    }

    override fun getName(): String = "Manage Tags and Ship Modes"

    override fun getStableLayoutName(): String = "Manage Tags and Ship Modes"
}
