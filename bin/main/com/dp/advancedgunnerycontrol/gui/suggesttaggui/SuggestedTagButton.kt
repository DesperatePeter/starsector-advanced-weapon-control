package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.dp.advancedgunnerycontrol.gui.AGCGUI
import com.dp.advancedgunnerycontrol.gui.ButtonBase
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.getSuggestedModesForWeaponId
import com.dp.advancedgunnerycontrol.typesandvalues.getTagTooltip
import com.dp.advancedgunnerycontrol.typesandvalues.isIncompatibleWithExistingTags
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class SuggestedTagButton(private val weaponId: String, tag: String, button: ButtonAPI) : ButtonBase<String>(tag, button, false) {
    companion object{
        fun createButtonGroup(weaponId: String, tooltip: TooltipMakerAPI, tagView: TagListView) : List<SuggestedTagButton>
        {
            val toReturn = mutableListOf<SuggestedTagButton>()
            tagView.view().forEach { tag ->
                toReturn.add(SuggestedTagButton(weaponId, tag, tooltip.addAreaCheckbox(
                    tag,
                    tag,
                    Misc.getBasePlayerColor(),
                    Misc.getDarkPlayerColor(),
                    Misc.getBrightPlayerColor(),
                    160f,
                    18f,
                    3f
                )))
                tooltip.addTooltipToPrevious(
                    AGCGUI.makeTooltip(getTagTooltip(tag)),
                    TooltipMakerAPI.TooltipLocation.BELOW
                )
                if(getSuggestedModesForWeaponId(weaponId).contains(tag)){
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
    override fun onActivate() {
        val st = Settings.getCurrentSuggestedTags().toMutableMap()
        st[weaponId] = ((st[weaponId] ?: listOf()) + listOf(associatedValue)).toSet().toList()
        Settings.customSuggestedTags = st
    }

    private fun onDeactivate() {
        val st = Settings.getCurrentSuggestedTags().toMutableMap()
        val l = (st[weaponId] ?: listOf())
        val l2 = l.toMutableList()
        l2.remove(associatedValue)
        st[weaponId] = l2
        Settings.customSuggestedTags = st
    }

    override fun executeCallbackIfChecked() {
        if (!active && button.isChecked) {
            check()
            updateDisabledButtons()
        } else if (active && !button.isChecked) {
            onDeactivate()
            uncheck()
            updateDisabledButtons()
        }
        button.isChecked = active
    }

    private fun updateDisabledButtons(){
        val tags = Settings.getCurrentSuggestedTags()[weaponId] ?: emptyList()
        sameGroupButtons.forEach {
            it.enable()
            if(isIncompatibleWithExistingTags(it.associatedValue, tags)){
                it.disable()
            }
        }
    }
}