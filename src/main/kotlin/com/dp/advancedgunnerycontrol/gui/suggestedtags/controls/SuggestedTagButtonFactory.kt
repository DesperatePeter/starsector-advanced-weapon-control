package com.dp.advancedgunnerycontrol.gui.suggestedtags.controls

import com.dp.advancedgunnerycontrol.gui.controls.buttons.LegacyAgcTooltipCheckbox
import com.dp.advancedgunnerycontrol.gui.controls.weapontags.CampaignTagToggleControls
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.weapontags.getTagTooltip
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

internal object SuggestedTagButtonFactory {
    private data class SuggestedTagButtonId(val weaponId: String, val tag: String)

    fun createButtonGroup(
        weaponId: String,
        tooltip: TooltipMakerAPI,
        tagView: TagListView,
    ): List<SuggestedTagButton> {
        val tagButtons = mutableListOf<SuggestedTagButton>()
        val selectedTags = SuggestedTagSelectionStore.supportedSuggestedTagsForWeapon(weaponId)
        val selectedTagSet = selectedTags.toSet()
        tagView.view().forEach { tag ->
            tagButtons.add(
                SuggestedTagButton(
                    weaponId,
                    tag,
                    LegacyAgcTooltipCheckbox.add(
                        tooltip = tooltip,
                        label = tag,
                        data = tag,
                        tooltipText = getTagTooltip(tag)
                    ),
                    selectedTagsSnapshot = selectedTags,
                )
            )
            if (tag in selectedTagSet) {
                tagButtons.last().setCheckedFromPersistence(true)
            }
        }
        tagButtons.forEach {
            it.connectSameGroupButtons(tagButtons)
        }
        tagButtons.firstOrNull()?.updateDisabledButtons(selectedTags)
        return tagButtons
    }

    fun createCampaignButtonGroup(
        weaponId: String,
        panel: CustomPanelAPI,
        visibleTags: List<String>,
        pinned: Boolean = false,
        selectedTagsOverride: List<String>? = null,
        unavailableTagsOverride: Set<String>? = null,
        onSelectionChanged: ((List<String>) -> Unit)? = null,
    ): List<SuggestedTagButton> {
        val tags = visibleTags
        val metrics = CampaignGuiStyle.campaignTagGridMetrics(tags, panel.position.width, panel.position.height)
        val selectedTags = selectedTagsOverride ?: SuggestedTagSelectionStore.supportedSuggestedTagsForWeapon(weaponId)
        val selectedTagSet = selectedTags.toSet()
        val unavailableTags = unavailableTagsOverride
            ?: SuggestedTagAvailability.unavailableTagsForSelection(tags.filterNot { it in selectedTagSet }, selectedTags)
        val tagButtons = mutableListOf<SuggestedTagButton>()

        tags.forEachIndexed { index, tag ->
            val unavailable = !pinned && tag in unavailableTags
            val shell = CampaignTagToggleControls.addTagToggleButton(
                parent = panel,
                data = SuggestedTagButtonId(weaponId, tag),
                tag = tag,
                index = index,
                metrics = metrics,
                tooltip = tooltipForTag(tag, unavailable, selectedTags),
                unavailable = unavailable,
                selected = tag in selectedTagSet
            )
            val suggestedButton = SuggestedTagButton(
                weaponId,
                tag,
                shell.button,
                selectedTagsSnapshot = selectedTags,
                unavailableTagsSnapshot = unavailableTags,
                onSelectionChanged = onSelectionChanged,
                useMomentaryVisualState = true,
            )
            tagButtons.add(suggestedButton)

            if (tag in selectedTagSet) {
                suggestedButton.setCheckedFromPersistence(true)
            }
        }

        tagButtons.forEach { it.connectSameGroupButtons(tagButtons) }
        tagButtons.firstOrNull()?.updateDisabledButtons(selectedTags)
        return tagButtons
    }

    private fun tooltipForTag(tag: String, unavailable: Boolean, selectedTags: List<String>): String {
        return SuggestedTagAvailability.tooltipForTag(tag, unavailable, selectedTags)
    }
}
