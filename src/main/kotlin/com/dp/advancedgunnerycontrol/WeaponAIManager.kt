package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.enums.FireMode
import com.dp.advancedgunnerycontrol.weaponais.*
import com.fs.starfarer.api.combat.AutofireAIPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponGroupAPI

class WeaponAIManager(private val engine: CombatEngineAPI) {
    var weaponGroupModes = HashMap<Int, WeaponModeSelector>()
    var weaponAIs = HashMap<WeaponAPI, AdjustableAIPlugin>()

    fun reset(){
        // Todo: truly revert to old plugin?
        weaponAIs.values.forEach{
            it.switchFireMode(FireMode.DEFAULT)
        }
        weaponGroupModes = HashMap()
        weaponAIs = HashMap()
    }

    /**
     * @return true if successful, false otherwise (e.g. index out of bounds)
     */
    fun cycleWeaponGroupMode(index: Int): Boolean {
        val ship = engine.playerShip ?: return false
        if (ship.weaponGroupsCopy.size <= index) return false
        val weaponGroup = ship.weaponGroupsCopy[index] ?: return false
        if(!weaponGroupModes.containsKey(index)){weaponGroupModes[index] = WeaponModeSelector()}
        weaponGroupModes[index]?.cycleMode() ?: return false
        weaponGroupModes[index]?.let {
            it.fractionOfWeaponsInMode  = adjustWeaponAIs(weaponGroup, it.currentMode)
        }
        return true
    }

    private fun adjustWeaponAIs(weaponGroup: WeaponGroupAPI, fireMode: FireMode) : Fraction {
        initializeAIsIfNecessary(weaponGroup.aiPlugins)
        var affectedWeapons = Fraction(0, weaponGroup.aiPlugins.size)
        weaponGroup.weaponsCopy.iterator().forEach { weapon ->
            if(weaponAIs[weapon]?.switchFireMode(fireMode) == true) affectedWeapons.numerator+=1
        }
        // TODO: Cleanup (Code's a bit messy)
        // This for-loop shouldn't be necessary from my understanding of how Kotlin works (every object is a reference)
        // But for some reason, the mod doesn't work properly without this loop, so...yeah...
        for(i in 0 until weaponGroup.aiPlugins.size){
            val weapon = weaponGroup.aiPlugins[i].weapon
            weaponAIs[weapon]?.let { weaponGroup.aiPlugins[i] = it }
        }
        return affectedWeapons
    }

    private fun initializeAIsIfNecessary(weaponAIList: MutableList<AutofireAIPlugin>) {
        for(i in 0 until weaponAIList.size){
            var weaponAI = weaponAIList[i]
            val weapon = weaponAI.weapon
            if ((weaponAI as? AdjustableAIPlugin) != null) continue // skip if already custom AI plugin
            if (null == weaponAIs[weapon]) weaponAIs[weapon] = AdjustableAIPlugin(weaponAI)
            weaponAIs[weapon]?.let { weaponAI = it }
        }
    }

    fun getFireModeDescription(groupNumber: Int): String {
         return "Group ${groupNumber+1}:" + getFireModeSuffix(groupNumber)
    }

    fun getFireModeSuffix(groupNumber: Int): String{
        return weaponGroupModes[groupNumber]?.currentModeAsString(groupNumber) ?: " --"

    }

}