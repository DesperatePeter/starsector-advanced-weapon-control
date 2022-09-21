package com.dp.advancedgunnerycontrol.typesandvalues

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCGridLayout
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

class TagListView {
    private val viewSize = (Global.getSettings().screenWidthPixels / (1.1f * (AGCGridLayout.buttonWidthPx + AGCGridLayout.paddingPx))).toInt()
    private var startingIndex = 0
    private var lastStartingIndex = startingIndex
    private val maxStartingIndex = max(Settings.tagList().size - viewSize - 1, 0)
    private fun endIndex() = min(startingIndex + viewSize, Settings.tagList().size - 1)
    private var lastEventTime: Long = 0
    fun advance(){
        if(Mouse.getEventNanoseconds() == lastEventTime) return
        lastEventTime = Mouse.getEventNanoseconds()
        val dw = Mouse.getEventDWheel()
        val delta = if(dw > 0) 1 else if(dw < 0) -1 else 0
        startingIndex = MathUtils.clamp(startingIndex - delta, 0, maxStartingIndex)
    }
    fun hasViewChanged(): Boolean{
        val hasChanged = lastStartingIndex != startingIndex
        lastStartingIndex = startingIndex
        return hasChanged
    }
    fun asciiScrollBar(): String{
        val hiddenTagsAtEnd = max(0, Settings.tagList().size - endIndex() - 1)
        if(startingIndex == 0 && hiddenTagsAtEnd == 0) return "All tags fit on screen"
        var toReturn = "-".repeat(startingIndex)
        toReturn += "<"
        toReturn += "=".repeat(endIndex()-startingIndex)
        toReturn += ">"
        toReturn += "-".repeat(hiddenTagsAtEnd)
        return toReturn
    }
    fun view(): List<String>{
        return Settings.tagList().subList(startingIndex, endIndex())
    }
}