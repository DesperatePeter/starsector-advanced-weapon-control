package com.dp.advancedgunnerycontrol.gui.actions

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignTooltipCopy

import com.dp.advancedgunnerycontrol.customlists.CustomWeaponTagListStore
import com.dp.advancedgunnerycontrol.customlists.WeaponTagListMode
import com.dp.advancedgunnerycontrol.gui.session.GUIAttributes
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext

class CycleTagListModeAction(attributes: GUIAttributes) : GUIAction(attributes) {
    companion object {
        private val MODES = listOf(
            WeaponTagListMode.CUSTOM_GLOBAL,
            WeaponTagListMode.CUSTOM,
            WeaponTagListMode.NOVICE,
            WeaponTagListMode.CLASSIC,
            WeaponTagListMode.COMPLETE
        )
        val STABLE_LAYOUT_NAME: String = labelFor(WeaponTagListMode.CUSTOM_GLOBAL)

        private fun labelFor(mode: WeaponTagListMode): String {
            return "Tag list: ${mode.displayName} [${MODES.indexOf(mode) + 1}/${MODES.size}]"
        }
    }

    override fun execute() {
        cycle(1)
    }

    override fun supportsRightClick(): Boolean = true

    override fun executeRightClick(): Boolean {
        cycle(-1)
        return true
    }

    private fun cycle(delta: Int) {
        val shipId = shipId() ?: return
        val next = adjacentMode(shipId, delta)
        CustomWeaponTagListStore.setActiveMode(shipId, next)
        if (!next.isCustom) {
            attributes.clearCustomListModalState()
        }
        attributes.tagView.reset()
    }

    override fun getTooltip(): String {
        val mode = shipId()?.let(::currentMode) ?: WeaponTagListMode.CUSTOM_GLOBAL
        return CampaignTooltipCopy.shipEditorTagListMode(mode)
    }

    override fun getName(): String {
        val mode = shipId()?.let(::currentMode) ?: WeaponTagListMode.CUSTOM_GLOBAL
        return labelFor(mode)
    }

    override fun getStableLayoutName(): String = STABLE_LAYOUT_NAME

    private fun currentMode(shipId: String): WeaponTagListMode {
        return CustomWeaponTagListStore.getActiveModeOrDefault(shipId)
    }

    private fun adjacentMode(shipId: String, delta: Int): WeaponTagListMode {
        val current = currentMode(shipId)
        val index = MODES.indexOf(current).coerceAtLeast(0)
        return MODES[(index + delta + MODES.size) % MODES.size]
    }

    private fun shipId(): String? {
        val ship = attributes.ship ?: return null
        return ShipEditorPersistenceContext(ship, attributes.runtimeShip).shipId.takeIf { it.isNotBlank() }
    }
}
