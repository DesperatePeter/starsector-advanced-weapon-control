package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import kotlin.math.ceil
import kotlin.math.min

class WeaponListView(private val viewSize: Int) {
    private val weaponIds
        get() = Global.getSector().allWeaponIds.sortedBy {
            nameFromId(it)
        }.filter { matchesFilters(it) }
    private var index = 0
        private set
    private var lastIndex = 0
    private val maxIndex
        get() = weaponIds.size - viewSize

    private var filters = mutableListOf<WeaponFilter>()

    val pageString
        get() = "Page ${ceil(index.toFloat()/viewSize).toInt() + 1}/${ceil(maxIndex.toFloat()/viewSize).toInt() + 1}"

    companion object{
        fun nameFromId(name: String): String = Global.getSettings()?.getWeaponSpec(name)?.weaponName ?: "Unknown"

    }

    fun toggleFilter(filter: WeaponFilter){
        if(filters.contains(filter)){
            filters.remove(filter)
        }else{
            filters.add(filter)
        }
        index = 0
    }

    fun containsFilter(filter: WeaponFilter): Boolean{
        return filters.contains(filter)
    }

    fun clearFilters(){
        filters.clear()
        index = 0
    }

    private fun matchesFilters(weapon: String): Boolean{
        return filters.groupBy { it.type() }.all {
            it.value.any { f -> f.matches(weapon) }
        }
    }

    fun cycle(){
        if(index == maxIndex) {
            index = 0
            return
        }
        index+= viewSize
        if(index > maxIndex) index = maxIndex
    }
    fun cycleBackwards(){
        if(index == 0){
            index = maxIndex
            return
        }
        index-= viewSize
        if(index < 0){
            index = 0
        }
    }
    fun currentIds(): List<String> = weaponIds.subList(index, min(index + viewSize, weaponIds.size))
    fun currentNames(): List<String> = currentIds().map { nameFromId(it) }
    fun hasChanged(): Boolean{
        if(index != lastIndex){
            lastIndex = index
            return true
        }
        return false
    }
}