package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
import com.dp.advancedgunnerycontrol.utils.SuffixStorage
import com.dp.advancedgunnerycontrol.utils.loadPersistentTags
import com.dp.advancedgunnerycontrol.utils.persistTags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class TagButton(ship: FleetMemberAPI, group : Int, tag : String, button: ButtonAPI) : ButtonBase<String>(ship, group, tag, button, false) {

    companion object{
        private var storage = Settings.tagStorage[AGCGUI.storageIndex]

        public fun createModeButtonGroup(ship: FleetMemberAPI, group: Int, tooltip: TooltipMakerAPI) : List<TagButton>{
            storage = Settings.tagStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<TagButton>()
            tags.forEach {
                toReturn.add(TagButton(ship, group, it, tooltip.addAreaCheckbox(it, it,
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 160f, 18f, 3f)))
                tooltip.addTooltipToPrevious(AGCGUI.makeTooltip(tagTooltips[it] ?: ""), TooltipMakerAPI.TooltipLocation.BELOW)
                if(loadPersistentTags(ship.id, group, AGCGUI.storageIndex).contains(it)){
                    toReturn.last().check()
                }
            }
            toReturn.forEach {
                it.sameGroupButtons = toReturn
            }
            return toReturn
        }
    }

    private fun updateDisabledButtons(){
        val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex)
        val tagsToDisable = mutableSetOf<String>()
        tags.forEach {
            tagIncompatibility[it]?.let { it1 -> tagsToDisable.addAll(it1) }
        }
        sameGroupButtons.forEach {
            it.enable()
            if(tagsToDisable.contains(it.associatedValue) || shouldTagBeDisabled(group, ship, it.associatedValue)){
                it.disable()
            }
        }
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked){
            check()
        } else if(active && !button.isChecked){
            val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex).toMutableList()
            tags.remove(associatedValue)
            persistTags(ship.id, group, AGCGUI.storageIndex, tags)
            uncheck()
        }
        button.isChecked = active
        updateDisabledButtons()
    }
    override fun onActivate() {
        val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex).toMutableList()
        tags.add(associatedValue)
        persistTags(ship.id, group, AGCGUI.storageIndex, tags)
    }
}