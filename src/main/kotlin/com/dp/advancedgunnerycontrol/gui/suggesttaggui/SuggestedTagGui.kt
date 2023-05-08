package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.dp.advancedgunnerycontrol.gui.GUIShower
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.lwjgl.input.Keyboard

class SuggestedTagGui : InteractionDialogPlugin {

    private var dialog : InteractionDialogAPI? = null
    private var view: SuggestedTagGuiView? = null
    private var tagListView = TagListView()
    private var weaponListView = WeaponListView(8)
    override fun init(i: InteractionDialogAPI?) {
        if(Settings.customSuggestedTags.isEmpty()) Settings.customSuggestedTags = Settings.defaultSuggestedTags
        dialog = i
        show()
    }

    private fun clear(){
        dialog?.textPanel?.clear()
        dialog?.visualPanel?.fadeVisualOut()
    }

    private fun show(){
        clear()
        showOptions()
        showView()
    }

    private fun resetTags(){
        Settings.customSuggestedTags = Settings.defaultSuggestedTags
        show()
    }

    private fun showOptions(){
        dialog?.optionPanel?.let {
            it.clearOptions()
            it.addOption("Next Page", "Next Page", "Display the next set of weapons.")
            it.setShortcut("Next Page", Keyboard.KEY_RIGHT, false, false, false, false)

            it.addOption("Previous Page", "Previous Page", "Display the previous set of weapons.")
            it.setShortcut("Previous Page", Keyboard.KEY_LEFT, false, false, false, false)

            it.addOption("Reset", "Reset", "Reset all suggested tags back to defaults.")

            if(Settings.autoApplySuggestedTags){
                it.addOption("Disable Auto-apply", "Disable Auto-apply",
                    "Suggested tags will no longer be applied automatically.")
            }else{
                it.addOption("Enable Auto-apply", "Enable Auto-apply",
                    "When activated, suggested tags will be applied automatically to any weapon group with no tags upon" +
                            "entering combat.")
            }


            it.addOption("Back", "Back")
            it.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)

            it.addOption("Exit", "Exit")
        }
    }

    private fun showView(){
        view = SuggestedTagGuiView(tagListView, weaponListView)
        dialog?.visualPanel?.let { view?.show(it) }
    }

    override fun optionSelected(str: String?, data: Any?) {
        str?.let {
            when(it){
                "Next Page" -> weaponListView.cycle()
                "Previous Page" -> weaponListView.cycleBackwards()
                "Reset" -> resetTags()
                "Back" -> {
                    dialog?.dismiss()
                    GUIShower.shouldOpenAgcGui = true
                }
                "Disable Auto-apply" -> {
                    Settings.autoApplySuggestedTags = false
                    show()
                }
                "Enable Auto-apply" -> {
                    Settings.autoApplySuggestedTags = true
                    show()
                }
                else -> dialog?.dismiss()
            }
        }
    }



    override fun advance(p0: Float) {
        if (tagListView.hasChanged() || weaponListView.hasChanged() ) show()
    }

    override fun optionMousedOver(p0: String?, p1: Any?) {}

    override fun backFromEngagement(p0: EngagementResultAPI?) {}

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}