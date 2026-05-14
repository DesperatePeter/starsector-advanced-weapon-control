package com.dp.advancedgunnerycontrol.weaponais.tags

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.shipdata.getAutofirePlugin
import com.dp.advancedgunnerycontrol.weaponais.TagBasedAI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI

data class DisableTagsToggleResult(
    val toggled: Boolean,
    val enabled: Boolean = false,
    val groupIndex: Int = -1,
    val reason: String = "",
)

object DisableTagsRuntime {
    fun toggleSelectedGroup(ship: ShipAPI): DisableTagsToggleResult {
        val groupIndex = selectedGroupIndex(ship)
        if (groupIndex < 0) {
            return DisableTagsToggleResult(toggled = false, reason = "No weapon group selected.")
        }
        if (!groupHasDisableTags(ship, groupIndex)) {
            return DisableTagsToggleResult(
                toggled = false,
                groupIndex = groupIndex,
                reason = "Selected weapon group does not have DisableTags."
            )
        }

        val disabledGroups = disabledGroups(ship)
        val enabled = if (groupIndex in disabledGroups) {
            disabledGroups.remove(groupIndex)
            false
        } else {
            disabledGroups.add(groupIndex)
            true
        }
        if (disabledGroups.isEmpty()) {
            ship.removeCustomData(Values.CUSTOM_SHIP_DATA_DISABLED_TAG_GROUPS_KEY)
        }
        return DisableTagsToggleResult(toggled = true, enabled = enabled, groupIndex = groupIndex)
    }

    fun isDisabled(weapon: WeaponAPI): Boolean {
        val ship = weapon.ship ?: return false
        val groupIndex = groupIndexForWeapon(ship, weapon)
        return groupIndex >= 0 && groupIndex in disabledGroups(ship)
    }

    fun disabledGroupIndices(ship: ShipAPI): Set<Int> = disabledGroups(ship).toSet()

    fun groupHasDisableTags(ship: ShipAPI, groupIndex: Int): Boolean {
        val group = ship.weaponGroupsCopy?.getOrNull(groupIndex) ?: return false
        for (weapon in group.weaponsCopy) {
            val ai = weapon.getAutofirePlugin() as? TagBasedAI ?: continue
            for (tag in ai.tags) {
                if (tag is DisableTagsTag) return true
            }
        }
        return false
    }

    private fun selectedGroupIndex(ship: ShipAPI): Int {
        val selectedGroup = ship.selectedGroupAPI ?: return -1
        return ship.weaponGroupsCopy?.indexOf(selectedGroup) ?: -1
    }

    private fun groupIndexForWeapon(ship: ShipAPI, weapon: WeaponAPI): Int {
        val group = ship.getWeaponGroupFor(weapon) ?: return -1
        return ship.weaponGroupsCopy?.indexOf(group) ?: -1
    }

    private fun disabledGroups(ship: ShipAPI): MutableSet<Int> {
        val existing = ship.customData[Values.CUSTOM_SHIP_DATA_DISABLED_TAG_GROUPS_KEY]
        if (existing is MutableSet<*>) {
            var valid = true
            for (entry in existing) {
                if (entry !is Int) {
                    valid = false
                    break
                }
            }
            if (valid) {
                @Suppress("UNCHECKED_CAST")
                return existing as MutableSet<Int>
            }
        }

        val disabledGroups = mutableSetOf<Int>()
        if (existing is Iterable<*>) {
            for (entry in existing) {
                if (entry is Int) disabledGroups.add(entry)
            }
        }
        ship.setCustomData(Values.CUSTOM_SHIP_DATA_DISABLED_TAG_GROUPS_KEY, disabledGroups)
        return disabledGroups
    }
}
