package com.dp.advancedgunnerycontrol

import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI

class WeaponAIManager(private val engine: CombatEngineAPI) {
    var weaponGroupModes = HashMap<Int, FireMode>()
    var weaponAIs = HashMap<WeaponAPI, AdvancedAIPlugin>()

    /**
     * @return true if successful, false otherwise (e.g. index out of bounds)
     */
    fun cycleWeaponGroupMode(index: Int): Boolean {
        val ship = engine.playerShip ?: return false
        if (ship.weaponGroupsCopy.size <= index) return false
        val weaponGroup = ship.weaponGroupsCopy[index] ?: return false

        val currentMode = weaponGroupModes[index] ?: FireMode.DEFAULT
        weaponGroupModes[index] = cycleFireMode(currentMode)
        weaponGroupModes[index]?.let { adjustWeaponAIs(weaponGroup, it) }
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
        return when (weaponGroupModes[groupNumber]) {
            FireMode.DEFAULT -> "${groupNumber+1}: |X---| Default"
            FireMode.PD -> "${groupNumber+1}: |-X--| PD Mode"
            FireMode.MISSILE -> "${groupNumber+1}: |--X-| Missiles only (experimental)"
            FireMode.FIGHTER -> "${groupNumber+1}: |---X| Fighters only (experimental)"
            else -> "${groupNumber+1}: Invalid Weapon Group"
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