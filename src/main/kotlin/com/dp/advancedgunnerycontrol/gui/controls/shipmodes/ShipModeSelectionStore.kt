package com.dp.advancedgunnerycontrol.gui.controls.shipmodes

import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object ShipModeSelectionStore {
    fun persistenceContext(
        ship: FleetMemberAPI,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): ShipEditorPersistenceContext {
        return persistenceContext ?: ShipEditorPersistenceContext(ship, runtimeShip)
    }

    fun loadModes(context: ShipEditorPersistenceContext): List<String> {
        return context.loadModes(AGCGUI.storageIndex)
    }

    fun loadModesForContext(
        ship: FleetMemberAPI,
        runtimeShip: ShipAPI?,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): List<String> {
        return loadModes(persistenceContext(ship, runtimeShip, persistenceContext))
    }

    fun saveModesForContext(
        ship: FleetMemberAPI,
        runtimeShip: ShipAPI?,
        modes: List<String>,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ) {
        persistenceContext(ship, runtimeShip, persistenceContext).saveModes(AGCGUI.storageIndex, modes)
    }

    fun applyPersistedModes(
        buttons: List<ShipModeButton>,
        ship: FleetMemberAPI,
        runtimeShip: ShipAPI?,
    ) {
        val modes = loadModesForContext(ship, runtimeShip)
        ShipModeButton.updateButtonsFromModes(buttons, modes)
    }
}
