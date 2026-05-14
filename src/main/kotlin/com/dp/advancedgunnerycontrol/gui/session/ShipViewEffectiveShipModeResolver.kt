package com.dp.advancedgunnerycontrol.gui.session

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI

internal class ShipViewEffectiveShipModeResolver(
    private val runtimeShip: ShipAPI?,
) {
    private var cacheKey: String? = null
    private var cachedModes: List<String>? = null

    fun clear() {
        cacheKey = null
        cachedModes = null
    }

    fun effectiveModesForEditor(
        ship: FleetMemberAPI,
        activeContext: ShipEditorPersistenceContext?,
    ): List<String> {
        val context = activeContext ?: ShipEditorPersistenceContext(ship, runtimeShip)
        val defaultModes = Settings.getCurrentShipModeNames()
        val customState = CustomWeaponTagListStore.getStateOrNull(context.shipId)
        val key = listOf(
            context.shipId,
            customState?.activeMode.orEmpty(),
            CustomWeaponTagListStore.currentListVersion().toString(),
            CustomShipModeListStore.currentListVersion().toString(),
            defaultModes.joinToString("|")
        ).joinToString("::")
        cachedModes?.let { cached ->
            if (cacheKey == key) return cached
        }
        return CustomShipModeListStore.effectiveModeNamesForShipOrDefault(context.shipId, defaultModes)
            .also {
                cacheKey = key
                cachedModes = it
            }
    }
}
