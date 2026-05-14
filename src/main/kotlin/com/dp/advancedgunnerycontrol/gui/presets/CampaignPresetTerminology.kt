package com.dp.advancedgunnerycontrol.gui.presets

import com.dp.advancedgunnerycontrol.gui.*


import com.dp.advancedgunnerycontrol.presets.WeaponPresetBackend
import com.dp.advancedgunnerycontrol.presets.WeaponPresetScope
import com.fs.starfarer.api.fleet.FleetMemberAPI

object CampaignPresetTerminology {
    const val CAMPAIGN_LOCKED_LABEL = "campaign save locked"
    const val CROSS_CAMPAIGN_LABEL = "cross-campaign"

    fun storageLabel(backend: WeaponPresetBackend): String {
        return when (backend) {
            WeaponPresetBackend.CAMPAIGN -> CAMPAIGN_LOCKED_LABEL
            WeaponPresetBackend.EXTERNAL -> CROSS_CAMPAIGN_LABEL
        }
    }

    fun bracketedStorageLabel(backend: WeaponPresetBackend): String = "(${storageLabel(backend)})"

    fun scopeLabel(ship: FleetMemberAPI?, scope: WeaponPresetScope): String {
        return when (scope) {
            WeaponPresetScope.SINGLE -> "ship-specific"
            WeaponPresetScope.CLASS -> "${ship?.hullSpec?.hullName ?: "ship-class"} class"
            WeaponPresetScope.GLOBAL -> "global"
            WeaponPresetScope.SUGGESTED -> "Suggested"
        }
    }

    fun presetDescriptorScopeLabel(scope: WeaponPresetScope): String {
        return when (scope) {
            WeaponPresetScope.SINGLE -> "ship-specific"
            WeaponPresetScope.CLASS -> "class-based"
            WeaponPresetScope.GLOBAL -> "global"
            WeaponPresetScope.SUGGESTED -> "suggested"
        }
    }

    fun presetFamilyTooltip(): String {
        return "Choose which preset family Save and Load use: this Single ship, this ship Class, Global presets, or Suggested weapon defaults."
    }

    fun noPresetTooltip(
        ship: FleetMemberAPI,
        groupIndex: Int,
        state: PresetControlState,
        allGroupsIndex: Int,
    ): String {
        val storage = storageLabel(state.backend)
        val scope = scopeLabel(ship, state.scope)
        return if (groupIndex == allGroupsIndex) {
            "You can't load presets because there are no matching $storage $scope presets for this ship's non-empty weapon groups."
        } else {
            "You can't load this preset because there is no $storage $scope preset for this weapon combination."
        }
    }
}
