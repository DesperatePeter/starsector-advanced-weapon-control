package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal object PresetControlStateMemory {
    private val statesByShip = mutableMapOf<String, MutableMap<Int, PresetControlState>>()

    fun stateFor(ship: FleetMemberAPI, groupIndex: Int): PresetControlState? {
        return shipKeys(ship).firstNotNullOfOrNull { key -> statesByShip[key]?.get(groupIndex) }
    }

    fun statesFor(ship: FleetMemberAPI): Map<Int, PresetControlState> {
        return shipKeys(ship)
            .mapNotNull { key -> statesByShip[key] }
            .fold(mutableMapOf<Int, PresetControlState>()) { merged, states ->
                merged.apply { putAll(states) }
            }
            .toMap()
    }

    fun remember(ship: FleetMemberAPI, groupIndex: Int, state: PresetControlState) {
        val stored = persistentSettingsOnly(state)
        shipKeys(ship).forEach { shipKey ->
            if (stored == PresetControlState()) {
                statesByShip[shipKey]?.remove(groupIndex)
                if (statesByShip[shipKey]?.isEmpty() == true) {
                    statesByShip.remove(shipKey)
                }
            } else {
                statesByShip.getOrPut(shipKey) { mutableMapOf() }[groupIndex] = stored
            }
        }
    }

    private fun persistentSettingsOnly(state: PresetControlState): PresetControlState {
        return state.copy(
            pendingAction = null,
            overwrite = false,
            cleanTags = null,
        )
    }

    private fun shipKeys(ship: FleetMemberAPI): List<String> {
        return listOf(
            agcStableShipId(ship),
            ship.id.orEmpty(),
        )
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }
}
