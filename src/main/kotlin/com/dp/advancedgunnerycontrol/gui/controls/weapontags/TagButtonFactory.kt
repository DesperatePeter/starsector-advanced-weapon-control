package com.dp.advancedgunnerycontrol.gui.controls.weapontags

import com.dp.advancedgunnerycontrol.gui.controls.buttons.LegacyAgcTooltipCheckbox
import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.shipdata.agcStableShipId
import com.dp.advancedgunnerycontrol.shipdata.loadPersistentTags
import com.dp.advancedgunnerycontrol.weapontags.getTagTooltip
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

internal object TagButtonFactory {
    private data class CampaignTagButtonId(val group: Int, val tag: String)

    fun createLegacyTooltipTagButtonGroup(
        ship: FleetMemberAPI,
        group: Int,
        tooltip: TooltipMakerAPI,
        tagView: TagListView,
    ): List<TagButton> {
        val tagButtons = mutableListOf<TagButton>()
        val persistedTags = loadPersistentTags(agcStableShipId(ship), group, AGCGUI.storageIndex)
        val persistedTagSet = persistedTags.toSet()
        tagView.view().forEach {
            tagButtons.add(
                TagButton(
                    ship, group, it, LegacyAgcTooltipCheckbox.add(
                        tooltip = tooltip,
                        label = it,
                        data = it,
                        tooltipText = getTagTooltip(it)
                    )
                )
            )
            if (it in persistedTagSet) {
                tagButtons.last().setCheckedFromPersistence(true)
            }
        }
        tagButtons.forEach {
            it.connectSameGroupButtons(tagButtons)
        }
        tagButtons.firstOrNull()?.updateDisabledButtons(persistedTags)
        return tagButtons
    }

    fun createCampaignTagButtonGroup(
        ship: FleetMemberAPI,
        group: Int,
        panel: CustomPanelAPI,
        renderedTags: List<String> = Settings.getCurrentWeaponTagList(),
        pinned: Boolean = false,
        runtimeShip: ShipAPI? = null,
        visibleTagsForSanitization: List<String> = renderedTags,
        onSelectionChanged: ((List<String>) -> Unit)? = null,
        sanitizedTagsOverride: List<String>? = null,
        unavailableTagsOverride: Set<String>? = null,
        persistenceContext: ShipEditorPersistenceContext? = null,
    ): List<TagButton> {
        val tags = renderedTags
        val sanitizedTags = sanitizedTagsOverride
            ?: repairAndPersistSelectedTags(
                ship = ship,
                group = group,
                runtimeShip = runtimeShip,
                visibleTags = visibleTagsForSanitization,
                persistenceContext = persistenceContext
            )
        val sanitizedTagSet = sanitizedTags.toSet()
        val unavailableTags = unavailableTagsOverride
            ?: unavailableTagsForSelection(ship, group, tags.filterNot { it in sanitizedTagSet }, sanitizedTags)
        val metrics = CampaignGuiStyle.campaignTagGridMetrics(tags, panel.position.width, panel.position.height)
        val tagButtons = mutableListOf<TagButton>()
        tags.forEachIndexed { index, tag ->
            val unavailable = !pinned && tag in unavailableTags
            val shell = CampaignTagToggleControls.addTagToggleButton(
                parent = panel,
                data = CampaignTagButtonId(group, tag),
                tag = tag,
                index = index,
                metrics = metrics,
                tooltip = tooltipForTag(ship, group, tag, unavailable, sanitizedTags),
                unavailable = unavailable,
                selected = tag in sanitizedTagSet
            )
            tagButtons.add(
                TagButton(
                    ship,
                    group,
                    tag,
                    shell.button,
                    runtimeShip,
                    visibleTagsForSanitization,
                    onSelectionChanged = onSelectionChanged,
                    selectedTagsSnapshot = sanitizedTags,
                    unavailableTagsSnapshot = unavailableTags,
                    persistenceContext = persistenceContext,
                    useMomentaryVisualState = true,
                )
            )
            if (tag in sanitizedTagSet) {
                tagButtons.last().setCheckedFromPersistence(true)
            }
        }
        tagButtons.forEach {
            it.connectSameGroupButtons(tagButtons)
        }
        return tagButtons
    }

    private fun repairAndPersistSelectedTags(
        ship: FleetMemberAPI,
        group: Int,
        runtimeShip: ShipAPI?,
        visibleTags: List<String>,
        persistenceContext: ShipEditorPersistenceContext?,
    ): MutableList<String> {
        return TagButton.repairAndPersistSelectedTags(
            ship = ship,
            group = group,
            runtimeShip = runtimeShip,
            visibleTags = visibleTags,
            persistenceContext = persistenceContext,
        )
    }

    private fun unavailableTagsForSelection(
        ship: FleetMemberAPI,
        group: Int,
        candidateTags: List<String>,
        currentTags: List<String>,
    ): Set<String> {
        return TagButton.unavailableTagsForSelection(ship, group, candidateTags, currentTags)
    }

    private fun tooltipForTag(
        ship: FleetMemberAPI,
        group: Int,
        tag: String,
        unavailable: Boolean,
        selectedTags: List<String>,
    ): String {
        return TagButtonAvailability.tooltipForTag(ship, group, tag, unavailable, selectedTags)
    }
}
