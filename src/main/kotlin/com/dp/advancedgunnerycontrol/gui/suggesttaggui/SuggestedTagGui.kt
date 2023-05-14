package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.dp.advancedgunnerycontrol.gui.GUIShower
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.TagListView
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import org.lwjgl.input.Keyboard
import java.awt.Color

class SuggestedTagGui : InteractionDialogPlugin {

    private var dialog : InteractionDialogAPI? = null
    private var view: SuggestedTagGuiView? = null
    private var tagListView = TagListView()
    private var weaponListView = WeaponListView(8)
    private var isFilterView = false
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

    private fun showDefaultOptions(){
        dialog?.optionPanel?.let {
            it.clearOptions()
            it.addOption("Next Page", "Next Page", "Display the next set of weapons.")
            it.setShortcut("Next Page", Keyboard.KEY_RIGHT, false, false, false, false)

            it.addOption("Previous Page", "Previous Page", "Display the previous set of weapons.")
            it.setShortcut("Previous Page", Keyboard.KEY_LEFT, false, false, false, false)

            it.addOption("Filter...", "Filter...", "Display filter options for weapon list." +
                    "\nWhen multiple filters of the same type are active, all weapons that match either will be shown." +
                    "\nNote: When filters are active, all options will only consider weapons that match filter criteria." +
                    " So make sure to reset filters before backing up or resetting.")
            it.setShortcut("Filter...", Keyboard.KEY_F, false, false, false, false)

            it.addOption("Reset", "Reset", "Reset all suggested tags back to defaults.")

            if(Settings.autoApplySuggestedTags){
                it.addOption("Disable Auto-apply", "Disable Auto-apply",
                    "Suggested tags will no longer be applied automatically.")
            }else{
                it.addOption("Enable Auto-apply", "Enable Auto-apply",
                    "When activated, suggested tags will be applied automatically to any weapon group with no tags upon" +
                            "entering combat.")
            }

            it.addOption("Backup", "Backup", "Save currently configured suggested tags to file (saves/common/${Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME})." +
                    "\nUse Restore to load saved values. This allows you to e.g. import these tags to another campaign." +
                    "\nYou can obviously also simply use this to back your current settings up in case you do something stupid." +
                    "\nNote: The underlying json parser is VERY liberal. So be careful when manually editing the file!")

            it.addOption("Restore", "Restore", "Load tags previously saved via Backup. Will overwrite any currently" +
                    "configured weapons that are present in the backup file.")


            it.addOption("Back", "Back")
            it.setShortcut("Back", Keyboard.KEY_ESCAPE, false, false, false, false)

            it.addOption("Exit", "Exit")
        }
    }

    private fun showFilterOptions(weaponList: WeaponListView){
        dialog?.optionPanel?.let { p ->
            p.clearOptions()
            p.addOption("Next Page", "Next Page", "Display the next set of weapons.")
            p.setShortcut("Next Page", Keyboard.KEY_RIGHT, false, false, false, false)

            p.addOption("Previous Page", "Previous Page", "Display the previous set of weapons.")
            p.setShortcut("Previous Page", Keyboard.KEY_LEFT, false, false, false, false)
            WeaponFilter.allFilters.forEach { f ->
                if (weaponList.containsFilter(f)){
                    p.addOption(f.name(), f, Color.GREEN, "Deactivate Filter")
                }else{
                    p.addOption(f.name(), f, "Activate Filter")
                }
            }
            p.addOption("Reset Filters", "Reset Filters")
            p.addOption("Back...", "Back...")
            p.setShortcut("Back...", Keyboard.KEY_ESCAPE, false, false, false, false)
        }
    }

    private fun showOptions(){
        if(isFilterView){
            showFilterOptions(weaponListView)
        }else{
            showDefaultOptions()
        }
    }

    private fun showView(){
        view = SuggestedTagGuiView(tagListView, weaponListView)
        dialog?.visualPanel?.let { view?.show(it) }
    }

    override fun optionSelected(str: String?, data: Any?) {
        (data as? WeaponFilter)?.let {
            weaponListView.toggleFilter(it)
            show()
            return
        }
        str?.let {
            when(it){
                "Next Page" -> weaponListView.cycle()
                "Previous Page" -> weaponListView.cycleBackwards()
                "Reset" -> resetTags()
                "Reset Filters" -> {
                    weaponListView.clearFilters()
                    show()
                }
                "Filter..." -> {
                    isFilterView = true
                    show()
                }
                "Back..." -> {
                    isFilterView = false
                    show()
                }
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
                "Backup" -> {
                    try {
                        backupSuggestedTagsToJson()
                    }catch (e: Throwable){
                        dialog?.textPanel?.clear()
                        dialog?.visualPanel?.fadeVisualOut()
                        dialog?.textPanel?.addPara("Ooops, something went wrong:" + e.message +
                            "\nIf you manually edited the file, delete/fix it. Otherwise, please file a bug report.", Color.RED)
                    }

                }
                "Restore" -> {
                    try {
                        restoreSuggestedTagsFromJson()
                        show()
                    }catch (e: Throwable){
                        dialog?.textPanel?.clear()
                        dialog?.visualPanel?.fadeVisualOut()
                        dialog?.textPanel?.addPara("Ooops, something went wrong:" + e.message +
                                "\nIf you manually edited the file, delete/fix it. Otherwise, please file a bug report.", Color.RED)
                    }

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