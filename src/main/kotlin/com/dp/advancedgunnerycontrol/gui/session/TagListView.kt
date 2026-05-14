package com.dp.advancedgunnerycontrol.gui.session

import com.dp.advancedgunnerycontrol.combatgui.agccombatgui.AGCGridLayout
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

class TagListView {
    private val viewSize = (
            Global.getSettings().screenWidthPixels /
                    (1.1f * (AGCGridLayout.buttonWidthPx + AGCGridLayout.paddingPx) * Global.getSettings().screenScaleMult)
            ).toInt()
    private var startingIndex = 0
    private var lastStartingIndex = startingIndex
    private val maxStartingIndex: Int
        get() = max(currentTags().size - viewSize - 1, 0)
    private var lastEventTime: Long = 0

    private fun currentTags(): List<String> = Settings.getCurrentWeaponTagList()

    private fun endIndex(tags: List<String>): Int {
        return min(startingIndex + viewSize, tags.size - 1)
    }

    fun advance() {
        if (Mouse.getEventNanoseconds() == lastEventTime) return
        lastEventTime = Mouse.getEventNanoseconds()
        val dw = Mouse.getEventDWheel()
        val delta = if (dw > 0) 1 else if (dw < 0) -1 else 0
        startingIndex = MathUtils.clamp(startingIndex - delta, 0, maxStartingIndex)
    }

    fun reset() {
        startingIndex = 0
    }

    fun hasChanged(): Boolean {
        val hasChanged = lastStartingIndex != startingIndex
        lastStartingIndex = startingIndex
        return hasChanged
    }

    fun asciiScrollBar(): String {
        val tags = currentTags()
        val endIndex = endIndex(tags)
        val hiddenTagsAtEnd = max(0, tags.size - endIndex - 1)
        if (startingIndex == 0 && hiddenTagsAtEnd == 0) return "All tags fit on screen"
        var scrollBar = "-".repeat(startingIndex)
        scrollBar += "<"
        scrollBar += "=".repeat(endIndex - startingIndex)
        scrollBar += ">"
        scrollBar += "-".repeat(hiddenTagsAtEnd)
        return scrollBar
    }

    fun view(): List<String> {
        // Note: sublist excludes the endIndex, i.e. goes until endIndex -1 ==> endIndex() + 1
        return try {
            val tags = currentTags()
            tags.subList(startingIndex, endIndex(tags) + 1)
        }catch (e: Exception){
            reset()
            val tags = currentTags()
            tags.subList(startingIndex, endIndex(tags) + 1)
        }

    }
}
