package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI

class WeaponAIManager(private val engine: CombatEngineAPI) {
    var weaponGroupModes = HashMap<Int, FireMode>()
    var weaponAIs = HashMap<WeaponAPI, PdAiPlugin>()

    fun cycleWeaponGroupMode(groupNumber: Int): Boolean {
        val currentMode = weaponGroupModes[groupNumber] ?: FireMode.DEFAULT
        weaponGroupModes[groupNumber] = cycleFireMode(currentMode)
        val ship = engine.playerShip ?: return false
        val weaponGroup = ship.weaponGroupsCopy[groupNumber] ?: return false
        weaponGroupModes[groupNumber]?.let { adjustWeaponAIs(weaponGroup, it) }
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
            if (null == weaponAIs[weapon]) weaponAIs[weapon] = PdAiPlugin(weapon, weaponAI)
            weaponAIs[weapon]?.let { weaponAI = it }
        }
    }

    fun getFireModeDescription(groupNumber: Int): String {
        return when (weaponGroupModes[groupNumber]) {
            FireMode.DEFAULT -> "${groupNumber+1}: Default"
            FireMode.PD -> "${groupNumber+1}: PD Mode"
            FireMode.MISSILE -> "${groupNumber+1}: Missiles only (experimental)"
            FireMode.FIGHTER -> "${groupNumber+1}: Fighters only (experimental)"
            else -> "Unknown"
        }
    }

    private fun cycleFireMode(fireMode: FireMode): FireMode {
        return when (fireMode) {
            FireMode.DEFAULT -> FireMode.PD
            FireMode.PD -> FireMode.MISSILE
            FireMode.MISSILE -> FireMode.FIGHTER
            FireMode.FIGHTER -> FireMode.DEFAULT
        }
    }
}