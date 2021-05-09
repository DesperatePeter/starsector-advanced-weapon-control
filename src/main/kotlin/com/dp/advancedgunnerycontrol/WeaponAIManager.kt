package com.dp.advancedgunnerycontrol

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.FireMode
import com.dp.advancedgunnerycontrol.typesandvalues.Suffixes
import com.dp.advancedgunnerycontrol.typesandvalues.createSuffix
import com.dp.advancedgunnerycontrol.utils.SuffixSelector
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
import com.dp.advancedgunnerycontrol.utils.WeaponModeSelector
import com.dp.advancedgunnerycontrol.weaponais.*
import com.dp.advancedgunnerycontrol.weaponais.suffixes.SuffixBase
import com.fs.starfarer.api.combat.*

class WeaponAIManager(private val engine: CombatEngineAPI, private var ship: ShipAPI?) {
    var weaponGroupModes = mutableMapOf<Int, WeaponModeSelector>()
        private set
    var weaponGroupSuffixes = mutableMapOf<Int, SuffixSelector>()
        private set
    private var weaponAIs = HashMap<WeaponAPI, AdjustableAIPlugin>()

    fun reset() {
        // Todo: truly revert to old plugin?
        weaponAIs.values.forEach {
            it.switchFireMode(FireMode.DEFAULT)
            it.setSuffix(Suffixes.NONE)
        }
        weaponGroupModes = mutableMapOf()
        weaponGroupSuffixes = mutableMapOf()
        weaponAIs = HashMap()
    }

    fun refresh(modesByGroup: MutableMap<Int, WeaponModeSelector>) {
        reset()
        weaponGroupModes = modesByGroup
        weaponGroupModes.forEach { (index, modeSelector) ->
            applyWeaponGroupMode(index, modeSelector)
        }
    }

    /**
     * @return true if successful, false otherwise (e.g. index out of bounds)
     * @param index: weapon group index (0..6)
     */
    fun cycleWeaponGroupMode(index: Int): Boolean {
        if (!weaponGroupModes.containsKey(index)) {
            weaponGroupModes[index] = WeaponModeSelector()
        }
        weaponGroupModes[index]?.cycle() ?: return false
        weaponGroupModes[index]?.let {
            return applyWeaponGroupMode(index, it)
        }
        return false
    }

    fun cycleSuffix(index: Int): Boolean{
        if(!weaponGroupSuffixes.containsKey(index)){
            weaponGroupSuffixes[index] = SuffixSelector()
        }
        val suffixSelector = weaponGroupSuffixes[index] ?: return false
        suffixSelector.cycle()
        if (null == ship) {
            ship = WeaponControlPlugin.determineSelectedShip(engine)
        }
        ship?.let { sh ->
            if (sh.weaponGroupsCopy.size <= index) return false
            val weaponGroup = sh.weaponGroupsCopy[index] ?: return false
            var successful = false
            weaponGroup.weaponsCopy.forEach {
                weaponAIs[it]?.run {
                    setSuffix(suffixSelector.currentValue)
                    successful = true
                }
            }
            return successful
        }
        return false
    }

    private fun applyWeaponGroupMode(index: Int, modeSelector: WeaponModeSelector): Boolean {
        if (null == ship) {
            ship = WeaponControlPlugin.determineSelectedShip(engine)
        }
        ship?.let { ship ->
            if (ship.weaponGroupsCopy.size <= index) return false
            val weaponGroup = ship.weaponGroupsCopy[index] ?: return false
            modeSelector.fractionOfWeaponsInMode = adjustWeaponAIs(weaponGroup, modeSelector.currentValue)
            if(Settings.skipInvalidModes() && modeSelector.currentValue != FireMode.DEFAULT && modeSelector.fractionOfWeaponsInMode.numerator == 0){
                return cycleWeaponGroupMode(index)
            }
            return true
        }
        return false
    }

    private fun adjustWeaponAIs(weaponGroup: WeaponGroupAPI, fireMode: FireMode): Fraction {
        initializeAIsIfNecessary(weaponGroup.aiPlugins)
        var affectedWeapons = Fraction(0, weaponGroup.aiPlugins.size)
        weaponGroup.weaponsCopy.iterator().forEach { weapon ->
            if (weaponAIs[weapon]?.switchFireMode(fireMode) == true) affectedWeapons.numerator += 1
        }

        // This for-loop shouldn't be necessary from my understanding of how Kotlin works (every object is a reference)
        // But for some reason, the mod doesn't work properly without this loop, so...yeah...
        for (i in 0 until weaponGroup.aiPlugins.size) {
            val weapon = weaponGroup.aiPlugins[i].weapon
            weaponAIs[weapon]?.let { weaponGroup.aiPlugins[i] = it }
        }
        return affectedWeapons
    }

    private fun initializeAIsIfNecessary(weaponAIList: MutableList<AutofireAIPlugin>) {
        for (i in 0 until weaponAIList.size) {
            var weaponAI = weaponAIList[i]
            val weapon = weaponAI.weapon
            if (((weaponAI as? AdjustableAIPlugin) != null) && (weaponAIs[weapon] != null)) continue // skip if already custom AI plugin
            val plugin = AdjustableAIPlugin(weaponAI)
            plugin.setSuffix(SuffixStorage.modesByShip[ship?.fleetMemberId]?.get(i))
            if (null == weaponAIs[weapon]) weaponAIs[weapon] = plugin
            weaponAIs[weapon]?.let { weaponAI = it }
        }
    }

    fun getFireModeDescription(groupNumber: Int): String {
        return "Group ${groupNumber + 1}:" + getFireModeSuffix(groupNumber)
    }

    fun getFireModeSuffix(groupNumber: Int): String {
        var suffixDescription = (weaponGroupSuffixes[groupNumber]?.currentValueAsString() ?: "")

        weaponGroupModes[groupNumber]?.currentValueAsString()?.let {
            return "$it $suffixDescription"
        }
        return ""
    }
}