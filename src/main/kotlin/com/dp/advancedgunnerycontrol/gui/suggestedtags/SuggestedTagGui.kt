package com.dp.advancedgunnerycontrol.gui.suggestedtags

import com.dp.advancedgunnerycontrol.gui.entrypoints.GUIShower
import com.dp.advancedgunnerycontrol.gui.suggestedtags.state.SuggestedTagEditorState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI

/**
 * Standalone campaign Customize Suggested Tags dialog.
 * Used when editing global/suggested weapon presets outside the ship-editor page.
 */
class SuggestedTagGui : InteractionDialogPlugin {
    private var dialog: InteractionDialogAPI? = null
    private val editorState = SuggestedTagEditorState()

    override fun init(interactionDialog: InteractionDialogAPI?) {
        editorState.ensureSuggestedTagsInitialized()
        dialog = interactionDialog
        openCustomEditor()
    }

    private fun openCustomEditor() {
        dialog?.textPanel?.clear()
        dialog?.optionPanel?.clearOptions()
        dialog?.hideTextPanel()
        dialog?.hideVisualPanel()
        dialog?.setPromptText("")
        val panelPlugin = SuggestedTagPanelPlugin(editorState, ::backToWeaponGroups)
        dialog?.showCustomVisualDialog(
            Global.getSettings().screenWidth.toFloat(),
            Global.getSettings().screenHeight.toFloat(),
            SuggestedTagDialogDelegate(panelPlugin)
        )
    }

    private fun backToWeaponGroups(callbacks: CustomVisualDialogDelegate.DialogCallbacks?) {
        callbacks?.dismissDialog()
        dialog?.dismiss()
        GUIShower.shouldOpenAgcGui = true
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {}
    override fun advance(amount: Float) {}
    override fun optionMousedOver(optionText: String?, optionData: Any?) {}
    override fun backFromEngagement(result: EngagementResultAPI?) {}
    override fun getContext(): Any? = null
    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}
