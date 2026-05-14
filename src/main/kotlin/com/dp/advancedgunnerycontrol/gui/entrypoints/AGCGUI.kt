package com.dp.advancedgunnerycontrol.gui.entrypoints

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*


import com.dp.advancedgunnerycontrol.gui.actions.ExitAction
import com.dp.advancedgunnerycontrol.gui.actions.GUIAction
import com.dp.advancedgunnerycontrol.gui.suggestedtags.SuggestedTagGui
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import org.lwjgl.input.Keyboard

/**
 * Top-level campaign AGC interaction dialog.
 * Shows the fleet-member picker and opens the campaign ShipView editor or the
 * standalone Customize Suggested Tags dialog.
 */
class AGCGUI : InteractionDialogPlugin {
    companion object {
        var storageIndex = Values.storageIndex //

        fun makeTooltip(description: String): TooltipMakerAPI.TooltipCreator {
            return object : TooltipMakerAPI.TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean = false
                override fun getTooltipWidth(tooltipParam: Any?): Float = CampaignGuiStyle.STANDARD_TOOLTIP_WIDTH
                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                    tooltip?.applyAgcTooltipTextStyle()
                    tooltip?.addAgcText(description, 5f)
                }
            }
        }
    }

    private var attributes = GUIAttributes()
    private var campaignEditorPanel: CampaignShipEditorPanelPlugin? = null
    private var pendingShipEditorOpen = false
    private var pendingShipEditorDelay = 0f
    private var guiHotkeyWasDown = false

    private fun addAction(action: GUIAction) {
        attributes.options?.addOption(action.getName(), action, action.getTooltip())
        action.getShortcut()?.let {
            attributes.options?.setShortcut(action, it, false, false, false, false)
        }
    }

    override fun init(dialog: InteractionDialogAPI?) {
        storageIndex = Values.storageIndex
        pendingShipEditorOpen = false
        pendingShipEditorDelay = 0f
        guiHotkeyWasDown = Keyboard.isKeyDown(Settings.guiHotkey())
        attributes.init(dialog)
        if (!Settings.enablePersistentModes()) {
            attributes.text?.addAgcText("Persistent Storage has been disabled in the settings.")
            attributes.text?.addAgcText("Enable it to use this GUI")
            addAction(ExitAction(attributes))
            return
        }
        val pendingShipId = GUIShower.pendingCampaignShipEditorShipId
        GUIShower.pendingCampaignShipEditorShipId = null
        if (pendingShipId != null) {
            val ship = editableCampaignShips()
                .firstOrNull { !it.isFighterWing && it.id == pendingShipId }
            if (ship != null) {
                attributes.ship = ship
                attributes.level = Level.SHIP
                pendingShipEditorOpen = true
                pendingShipEditorDelay = 0.05f
                return
            }
        }
        displayOptions()
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        (optionData as? GUIAction)?.execute()
        displayOptions()
        return
    }

    private fun displayOptions() {
        attributes.options?.clearOptions()
        when (attributes.level) {
            Level.TOP -> displayFleetOptions()
            Level.SHIP -> openShipEditor()
        }
    }

    private fun displayFleetOptions() {
        pendingShipEditorOpen = false
        pendingShipEditorDelay = 0f
        clear()
        attributes.dialog?.showTextPanel()
        attributes.dialog?.showVisualPanel()
        val ships = editableCampaignShips()
        if (ships.isEmpty()) {
            attributes.text?.addAgcText("No editable ships are available.")
            addAction(ExitAction(attributes))
            return
        }
        attributes.dialog?.showFleetMemberPickerDialog("Pick a ship to adjust weapon modes & suffixes for",
            "Confirm",
            "Exit",
            5,
            6,
            100f,
            true,
            false,
            ships,
            object : FleetMemberPickerListener {
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    selected?.firstOrNull()?.let {
                        attributes.ship = it
                        attributes.level = Level.SHIP
                        pendingShipEditorOpen = true
                        pendingShipEditorDelay = 0.05f
                        Global.getLogger(AGCGUI::class.java).info(
                            "[AGC_CAMPAIGN_UI] queued ship editor open ship=${it.shipName} hull=${it.variant?.hullVariantId}"
                        )
                        return
                    } ?: attributes.dialog?.dismiss()
                }

                override fun cancelledFleetMemberPicking() {
                    attributes.dialog?.dismiss()
                }
            })
    }

    private fun editableCampaignShips(): List<FleetMemberAPI> {
        return Global.getSector()?.playerFleet?.membersWithFightersCopy
            ?.filter { !it.isFighterWing }
            .orEmpty()
    }

    private fun openShipEditor() {
        clear()
        attributes.options?.clearOptions()
        attributes.dialog?.hideTextPanel()
        attributes.dialog?.hideVisualPanel()
        attributes.dialog?.setPromptText("")
        Global.getLogger(AGCGUI::class.java).info(
            "[AGC_CAMPAIGN_UI] openShipEditor ship=${attributes.ship?.shipName} hull=${attributes.ship?.variant?.hullVariantId}"
        )
        campaignEditorPanel = CampaignShipEditorPanelPlugin(attributes) {
            attributes.level = Level.TOP
            displayFleetOptions()
        }
        attributes.dialog?.showCustomVisualDialog(
            Global.getSettings().screenWidth.toFloat(),
            Global.getSettings().screenHeight.toFloat(),
            CampaignShipEditorDialogDelegate(campaignEditorPanel ?: return)
        )
    }

    private fun clear() {
        attributes.text?.clear()
        attributes.visualPanel?.fadeVisualOut()
    }

    override fun optionMousedOver(optionString: String?, optionData: Any?) {}
    override fun advance(amount: Float) {
        if (closeShipSelectOnGuiHotkey()) return
        if (!pendingShipEditorOpen) return
        pendingShipEditorDelay -= amount
        if (pendingShipEditorDelay > 0f) return
        pendingShipEditorOpen = false
        displayOptions()
    }

    private fun closeShipSelectOnGuiHotkey(): Boolean {
        val guiHotkeyDown = Keyboard.isKeyDown(Settings.guiHotkey())
        val shouldClose = attributes.level == Level.TOP &&
            !pendingShipEditorOpen &&
            guiHotkeyDown &&
            !guiHotkeyWasDown
        guiHotkeyWasDown = guiHotkeyDown
        if (!shouldClose) return false
        attributes.dialog?.dismiss()
        return true
    }

    override fun backFromEngagement(result: EngagementResultAPI?) {}
    override fun getContext(): Any? = null
    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}
