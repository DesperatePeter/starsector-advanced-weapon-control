package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.fs.starfarer.api.Global
import kotlin.math.ceil

class WeaponListView(private val viewSize: Int) {
    private val weaponIds = Global.getSector().allWeaponIds.sortedBy {
        nameFromId(it)
    }
    private var index = 0
        private set
    private var lastIndex = 0
    private val maxIndex
        get() = weaponIds.size - viewSize

    val pageString
        get() = "Page ${ceil(index.toFloat()/viewSize).toInt() + 1}/${ceil(maxIndex.toFloat()/viewSize).toInt() + 1}"

    companion object{
        fun nameFromId(name: String): String = Global.getSettings()?.getWeaponSpec(name)?.weaponName ?: "Unknown"
        fun paddedName(name: String): String {
            var toReturn = nameFromId(name)
            toReturn += if (toReturn.length < 20) "\n" else ""
            return toReturn
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
    fun currentIds(): List<String> = weaponIds.subList(index, index + viewSize)
    fun currentNames(): List<String> = currentIds().map { nameFromId(it) }
    fun hasChanged(): Boolean{
        if(index != lastIndex){
            lastIndex = index
            return true
        }
        return false
    }
}