package com.dp.advancedgunnerycontrol.gui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip
import com.dp.advancedgunnerycontrol.typesandvalues.isIncompatibleWithExistingTags
import com.dp.advancedgunnerycontrol.typesandvalues.shouldTagBeDisabled
import com.dp.advancedgunnerycontrol.utils.loadPersistentTags
import com.dp.advancedgunnerycontrol.utils.persistTags
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class TagButton(var ship: FleetMemberAPI, var group: Int, tag: String, button: ButtonAPI) :
    ButtonBase<String>(tag, button, false) {

    companion object {
        private var storage = Settings.tagStorage[AGCGUI.storageIndex]

        fun createModeButtonGroup(
            ship: FleetMemberAPI,
            group: Int,
            tooltip: TooltipMakerAPI,
            tagView: TagListView
        ): List<TagButton> {
            storage = Settings.tagStorage[AGCGUI.storageIndex]
            val toReturn = mutableListOf<TagButton>()
            tagView.view().forEach {
                toReturn.add(
                    TagButton(
                        ship, group, it, tooltip.addAreaCheckbox(
                            it,
                            it,
                            Misc.getBasePlayerColor(),
                            Misc.getDarkPlayerColor(),
                            Misc.getBrightPlayerColor(),
                            160f,
                            18f,
                            3f
                        )
                    )
                )
                tooltip.addTooltipToPrevious(
                    AGCGUI.makeTooltip(getTagTooltip(it)),
                    TooltipMakerAPI.TooltipLocation.BELOW
                )
                if (loadPersistentTags(ship.id, ship, group, AGCGUI.storageIndex).contains(it)) {
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

    private fun updateDisabledButtons() {
        val tags = loadPersistentTags(ship.id, ship, group, AGCGUI.storageIndex).toMutableList()
        sameGroupButtons.forEach {
            it.enable()
            if (isIncompatibleWithExistingTags(it.associatedValue, tags) || shouldTagBeDisabled(
                    group,
                    ship,
                    it.associatedValue
                )
            ) {
                tags.remove(it.associatedValue)
                it.disable()
                it.button.isChecked = false
            }
        }
        persistTags(ship.id, ship, group, AGCGUI.storageIndex, tags)
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked) {
            check()
            updateDisabledButtons()
        } else if (active && !button.isChecked) {
            val tags = loadPersistentTags(ship.id, ship, group, AGCGUI.storageIndex).toMutableList()
            tags.remove(associatedValue)
            persistTags(ship.id, ship, group, AGCGUI.storageIndex, tags)
            uncheck()
            updateDisabledButtons()
        }
        button.isChecked = active
    }

    override fun onActivate() {
        val tags = loadPersistentTags(ship.id, ship, group, AGCGUI.storageIndex).toMutableList()
        tags.add(associatedValue)
        persistTags(ship.id, ship, group, AGCGUI.storageIndex, tags)
    }
}