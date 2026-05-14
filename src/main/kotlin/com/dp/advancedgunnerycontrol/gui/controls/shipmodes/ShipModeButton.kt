package com.dp.advancedgunnerycontrol.gui.controls.shipmodes

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleVisualState
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.shipmodes.defaultShipMode
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI

/**
 * Visible ship-mode row component.
 * Used in the Ship Modes panel to toggle, pin, and right-click edit modes for
 * the current ship/loadout.
 */
class ShipModeButton(
    var ship: FleetMemberAPI,
    mode: String,
    button: ButtonAPI,
    private val runtimeShip: ShipAPI? = null,
    private val onSelectionChanged: (() -> Unit)? = null,
    private val currentModesSnapshot: List<String>? = null,
    private val persistenceContext: ShipEditorPersistenceContext? = null,
    useMomentaryVisualState: Boolean = false,
) : ButtonBase<String>(mode, button, false, useMomentaryVisualState) {
    private val visualState = CampaignToggleVisualState()

    companion object {
        var campaignShipModeSelectionVersion = 0
            private set

        fun notifyCampaignShipModeSelectionChanged() {
            campaignShipModeSelectionVersion++
        }

        fun createModeButtonGroup(
            ship: FleetMemberAPI,
            panel: CustomPanelAPI,
            position: UIComponentAPI,
        ): List<ShipModeButton> {
            return ShipModeButtonFactory.createModeButtonGroup(ship, panel, position)
        }

        fun createCampaignModeButtonGroup(
            ship: FleetMemberAPI,
            panel: CustomPanelAPI,
            runtimeShip: ShipAPI? = null,
            rowOffset: Int = 0,
            onScrollRows: (Int) -> Unit = {},
            layoutContainerHeight: Float = panel.position.height,
            onSelectionChanged: (() -> Unit)? = null,
            preparedGroup: PreparedCampaignModeButtonGroup? = null,
            persistenceContext: ShipEditorPersistenceContext? = null,
        ): CampaignModeButtonGroupResult {
            return ShipModeButtonFactory.createCampaignModeButtonGroup(
                ship = ship,
                panel = panel,
                runtimeShip = runtimeShip,
                rowOffset = rowOffset,
                onScrollRows = onScrollRows,
                layoutContainerHeight = layoutContainerHeight,
                onSelectionChanged = onSelectionChanged,
                preparedGroup = preparedGroup,
                persistenceContext = persistenceContext,
            )
        }

        fun prepareCampaignModeButtonGroup(
            ship: FleetMemberAPI,
            width: Float,
            availableHeight: Float,
            runtimeShip: ShipAPI? = null,
            rowOffset: Int = 0,
            configuredModesOverride: List<String>? = null,
            persistenceContext: ShipEditorPersistenceContext? = null,
            forceActiveSectionExpanded: Boolean = false,
        ): PreparedCampaignModeButtonGroup {
            return ShipModeButtonPlanner.prepareCampaignModeButtonGroup(
                ship = ship,
                width = width,
                availableHeight = availableHeight,
                runtimeShip = runtimeShip,
                rowOffset = rowOffset,
                configuredModesOverride = configuredModesOverride,
                persistenceContext = persistenceContext,
                forceActiveSectionExpanded = forceActiveSectionExpanded,
            )
        }

        fun estimateCampaignModeButtonGroupTightHeight(
            ship: FleetMemberAPI,
            width: Float,
            availableHeight: Float,
            runtimeShip: ShipAPI? = null,
            rowOffset: Int = 0,
            preparedGroup: PreparedCampaignModeButtonGroup? = null,
        ): Float {
            return ShipModeButtonPlanner.estimateCampaignModeButtonGroupTightHeight(
                ship = ship,
                width = width,
                availableHeight = availableHeight,
                runtimeShip = runtimeShip,
                rowOffset = rowOffset,
                preparedGroup = preparedGroup,
            )
        }

        internal fun updateButtonsFromModes(
            buttons: List<ShipModeButton>,
            modes: List<String>,
        ) {
            buttons.forEach { it.updateCheckedFromModes(modes) }
        }
    }

    override fun executeCallbackIfChecked(): Boolean {
        if (useMomentaryVisualState) {
            return executeMomentaryVisualCallbackIfClicked()
        }
        if (!active && button.isChecked) {
            val modes = currentModesForClick()
            val modeName = associatedValue
            val updatedModes = if (modeName == defaultShipMode) {
                listOf(defaultShipMode)
            } else {
                (modes.filterNot { it == defaultShipMode } + modeName).distinct()
            }
            saveCurrentModes(updatedModes)
            setActiveChecked(true)
            onSelectionChanged?.invoke() ?: run {
                campaignShipModeSelectionVersion++
                updateSameGroupFromModes(updatedModes)
            }
            syncButtonCheckedToActive()
            applyToggleableVisualState()
            return true
        } else if (active && !button.isChecked) {
            val modeName = associatedValue
            val modes = currentModesForClick().filterNot { it == modeName }
            saveCurrentModes(modes)
            uncheck()
            onSelectionChanged?.invoke() ?: run {
                campaignShipModeSelectionVersion++
                updateSameGroupFromModes(modes)
            }
            syncButtonCheckedToActive()
            applyToggleableVisualState()
            return true
        }
        syncButtonCheckedToActive()
        applyToggleableVisualState()
        return false
    }

    private fun executeMomentaryVisualCallbackIfClicked(): Boolean {
        return executeMomentaryVisualToggleIfClicked(
            currentState = ::currentModesForClick,
            updatedState = { currentlyActive, modes ->
                val modeName = associatedValue
                if (currentlyActive) {
                    modes.filterNot { it == modeName }
                } else if (modeName == defaultShipMode) {
                    listOf(defaultShipMode)
                } else {
                    (modes.filterNot { it == defaultShipMode } + modeName).distinct()
                }
            },
            saveState = ::saveCurrentModes,
            afterStateChanged = ::afterModesChanged,
        )
    }

    private fun applyToggleableVisualState(force: Boolean = false) {
        visualState.apply(button, force)
    }

    private fun updateSameGroupFromModes(modes: List<String>) {
        sameGroupButtons.forEach { (it as? ShipModeButton)?.updateCheckedFromModes(modes) }
    }

    internal fun updateCheckedFromModes(modes: List<String>) {
        if (useMomentaryVisualState) {
            setActiveForConfiguredVisualState(modes.contains(associatedValue))
            return
        }
        if (modes.contains(associatedValue)) {
            check()
        } else {
            uncheck()
        }
        applyToggleableVisualState()
    }

    private fun loadCurrentModes(): List<String> {
        return ShipModeSelectionStore.loadModesForContext(ship, runtimeShip, persistenceContext)
    }

    private fun currentModesForClick(): List<String> {
        return currentModesSnapshot ?: loadCurrentModes()
    }

    private fun saveCurrentModes(modes: List<String>) {
        ShipModeSelectionStore.saveModesForContext(ship, runtimeShip, modes, persistenceContext)
    }

    private fun afterModesChanged(modes: List<String>) {
        onSelectionChanged?.invoke() ?: run {
            campaignShipModeSelectionVersion++
            updateSameGroupFromModes(modes)
        }
    }

    override fun onActivate() {
    }
}
