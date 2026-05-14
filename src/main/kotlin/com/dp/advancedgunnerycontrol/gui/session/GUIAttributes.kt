package com.dp.advancedgunnerycontrol.gui.session

import com.dp.advancedgunnerycontrol.config.*
import com.dp.advancedgunnerycontrol.customlists.*
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.shipmodes.*
import com.dp.advancedgunnerycontrol.weapontags.*


import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.campaign.VisualPanelAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI

enum class CustomListModalMode {
    MANAGE_TAGS,
    EDIT_TAG,
    CONFIRM_TAG_CHANGES,
    DEBUG_COLORS,
    RENAME_LOADOUT,
}

class GUIAttributes {
    var level = Level.TOP
    var ship: FleetMemberAPI? = null
    var dialog: InteractionDialogAPI? = null
    var text: TextPanelAPI? = null
    var options: OptionPanelAPI? = null
    var visualPanel: VisualPanelAPI? = null
    var customPanel: CustomPanelAPI? = null
    var tagView = TagListView()
    var runtimeShip: ShipAPI? = null
    var customListModalMode: CustomListModalMode? = null
    var customListDraftDefinitionId: String? = null
    var customListDraftValues: MutableMap<String, String> = mutableMapOf()
    var customListEditSourceGroup: Int? = null
    var customListEditSourceTag: String? = null
    var customListManagerScrollOffset = 0
    var customListChangeReviewScrollOffset = 0
    var customTagsMarkedForRemoval: MutableSet<String> = mutableSetOf()

    fun init(input: InteractionDialogAPI?) {
        input?.let {
            dialog = it
            text = it.textPanel
            options = it.optionPanel
            visualPanel = it.visualPanel
            visualPanel?.saveCurrentVisual()
        }
    }

    fun clearCustomListModalState() {
        customListModalMode = null
        customListDraftDefinitionId = null
        customListDraftValues.clear()
        customListEditSourceGroup = null
        customListEditSourceTag = null
        customListManagerScrollOffset = 0
        customListChangeReviewScrollOffset = 0
        customTagsMarkedForRemoval.clear()
    }
}

data class ShipEditorCapabilities(
    val canSelectOtherShips: Boolean,
    val canAffectFleet: Boolean,
    val canAffectAllLoadouts: Boolean,
    val canCustomizeSuggestedTags: Boolean,
    val canReloadSettings: Boolean,
    val canCycleLoadout: Boolean = true,
) {
    fun modifierText(): String? {
        val lines = mutableListOf<String>()
        if (canAffectFleet) {
            lines.add("[SHIFT] = FLEET")
        }
        if (canAffectAllLoadouts) {
            lines.add("[CTRL] = ALL LOADOUTS")
        }
        if (lines.isEmpty()) return null
        return "MODIFIERS:\n${lines.joinToString("\n")}"
    }

    companion object {
        val CAMPAIGN = ShipEditorCapabilities(
            canSelectOtherShips = true,
            canAffectFleet = true,
            canAffectAllLoadouts = true,
            canCustomizeSuggestedTags = true,
            canReloadSettings = true,
        )

        val DIRECT = ShipEditorCapabilities(
            canSelectOtherShips = true,
            canAffectFleet = true,
            canAffectAllLoadouts = true,
            canCustomizeSuggestedTags = true,
            canReloadSettings = true,
        )
    }
}
