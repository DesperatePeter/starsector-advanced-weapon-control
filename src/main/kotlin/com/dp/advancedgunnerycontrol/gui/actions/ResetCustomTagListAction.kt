package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.customlists.CustomWeaponTagListStore
import com.dp.advancedgunnerycontrol.customlists.WeaponTagListMode
import com.dp.advancedgunnerycontrol.customlists.removeTagsFromAllShipLoadouts
import com.dp.advancedgunnerycontrol.gui.modals.CampaignConfirmationTone
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

class ResetCustomTagListAction(attributes: GUIAttributes) : GUIAction(attributes) {
    override fun execute() {
        val ship = attributes.ship ?: return
        val context = ShipEditorPersistenceContext(ship, attributes.runtimeShip)
        val shipId = context.shipId.takeIf { it.isNotBlank() } ?: return
        val classicTags = Settings.getWeaponTagListForMode(WeaponTagListMode.CLASSIC)
        val removedTags = CustomWeaponTagListStore.getStoredCustomTags(shipId).toSet() - classicTags.toSet()

        CustomWeaponTagListStore.resetCustomTagsToClassic(shipId)
        removeTagsFromAllShipLoadouts(context, removedTags)
        attributes.clearCustomListModalState()
        attributes.tagView.reset()
    }

    fun isVisible(): Boolean {
        val ship = attributes.ship ?: return false
        val shipId = ShipEditorPersistenceContext(ship, attributes.runtimeShip).shipId.takeIf { it.isNotBlank() } ?: return false
        return CustomWeaponTagListStore.getActiveModeOrDefault(shipId).isCustom
    }

    override fun getTooltip(): String {
        return "Reset the ${activeListLabel()} Custom list to match Classic. The other Custom list is not changed. Tags that only exist in this list are removed from this ship's weapon groups and loadouts."
    }

    override fun getConfirmationDescription(): String {
        return "Confirming will reset the ${activeListLabel()} Custom list to match Classic without changing the other Custom list. Tags that only exist in this list will be removed from this ship's weapon groups and loadouts."
    }

    override fun requiresConfirmation(): Boolean = true

    override fun getConfirmationTitle(): String = "Reset Customised Tags Warning"

    override fun getConfirmationTone(): CampaignConfirmationTone = CampaignConfirmationTone.WARNING

    override fun getName(): String = "Reset ${activeListLabel()} List"

    override fun getStableLayoutName(): String = "Reset \"All Ships\" List"

    private fun activeListLabel(): String {
        val ship = attributes.ship ?: return "\"This Ship\""
        val shipId = ShipEditorPersistenceContext(ship, attributes.runtimeShip).shipId
            .takeIf { it.isNotBlank() }
            ?: return "\"This Ship\""
        return when (CustomWeaponTagListStore.getActiveModeOrDefault(shipId)) {
            WeaponTagListMode.CUSTOM_GLOBAL -> "\"All Ships\""
            else -> "\"This Ship\""
        }
    }
}
