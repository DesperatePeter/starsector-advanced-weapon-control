package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI

class WeaponAIManager(private val engine: CombatEngineAPI) {
    var weaponGroupModes = HashMap<Int, WeaponModeSelector>()
    var weaponAIs = HashMap<WeaponAPI, AdvancedAIPlugin>()

    /**
     * @return true if successful, false otherwise (e.g. index out of bounds)
     */
    fun cycleWeaponGroupMode(index: Int): Boolean {
        val ship = engine.playerShip ?: return false
        if (ship.weaponGroupsCopy.size <= index) return false
        val weaponGroup = ship.weaponGroupsCopy[index] ?: return false
        if(!weaponGroupModes.containsKey(index)){weaponGroupModes[index] = WeaponModeSelector()}
        weaponGroupModes[index]?.cycleMode() ?: return false
        weaponGroupModes[index]?.let { adjustWeaponAIs(weaponGroup, it.currentMode) }
        return true
    }

    private fun adjustWeaponAIs(weaponGroup: WeaponGroupAPI, fireMode: FireMode) {
        initializeAIsIfNecessary(weaponGroup.aiPlugins)
        weaponGroup.weaponsCopy.iterator().forEach { weapon ->
            weaponAIs[weapon]?.fireMode = fireMode
        }
        for(i in 0 until weaponGroup.aiPlugins.size){
            val weapon = weaponGroup.aiPlugins[i].weapon
            weaponAIs[weapon]?.let { weaponGroup.aiPlugins[i] = it }
        }
    }

    private fun initializeAIsIfNecessary(weaponAIList: MutableList<AutofireAIPlugin>) {
        for(i in 0 until weaponAIList.size){
            var weaponAI = weaponAIList[i]
            val weapon = weaponAI.weapon
            if (null == weaponAIs[weapon]) weaponAIs[weapon] = AdvancedAIPlugin(weapon, weaponAI)
            weaponAIs[weapon]?.let { weaponAI = it }
        }
    }

    fun getFireModeDescription(groupNumber: Int): String {
        return weaponGroupModes[groupNumber]?.currentModeAsString(groupNumber) ?: "Unknown Weapon Group =/"
    }

}