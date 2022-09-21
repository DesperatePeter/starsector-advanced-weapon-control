package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.*
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

        public fun createModeButtonGroup(ship: FleetMemberAPI, group: Int, tooltip: TooltipMakerAPI, tagView: TagListView) : List<TagButton>{
            storage = Settings.tagStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<TagButton>()
            tagView.view().forEach {
                toReturn.add(TagButton(ship, group, it, tooltip.addAreaCheckbox(it, it,
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 160f, 18f, 3f)))
                tooltip.addTooltipToPrevious(AGCGUI.makeTooltip(getTagTooltip(it)), TooltipMakerAPI.TooltipLocation.BELOW)
                if(loadPersistentTags(ship.id, group, AGCGUI.storageIndex).contains(it)){
                    toReturn.last().check()
                }
            }
            toReturn.forEach {
                it.sameGroupButtons = toReturn
            }
            toReturn.forEach {
                it.updateDisabledButtons()
            }
            return toReturn
        }
    }

    private fun updateDisabledButtons(){
        val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex)
        sameGroupButtons.forEach {
            it.enable()
            if(isIncompatibleWithExistingTags(it.associatedValue, tags) || shouldTagBeDisabled(group, ship, it.associatedValue)){
                it.disable()
            }
        }
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked){
            check()
            updateDisabledButtons()
        } else if(active && !button.isChecked){
            val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex).toMutableList()
            tags.remove(associatedValue)
            persistTags(ship.id, group, AGCGUI.storageIndex, tags)
            uncheck()
            updateDisabledButtons()
        }
        button.isChecked = active
    }
    override fun onActivate() {
        val tags = loadPersistentTags(ship.id, group, AGCGUI.storageIndex).toMutableList()
        tags.add(associatedValue)
        persistTags(ship.id, group, AGCGUI.storageIndex, tags)
    }
}