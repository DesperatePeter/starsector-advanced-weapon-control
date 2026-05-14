package com.dp.advancedgunnerycontrol.gui.controls.weapontags

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleVisualState
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.shipdata.ShipEditorPersistenceContext
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

/**
 * Visible weapon-tag row component.
 * Used in weapon-group tag lists and legacy tooltip paths to toggle one tag for
 * one weapon group.
 */
class TagButton(
    var ship: FleetMemberAPI,
    var group: Int,
    tag: String,
    button: ButtonAPI,
    private val runtimeShip: ShipAPI? = null,
    private val visibleTagsForSanitization: List<String> = Settings.getCurrentWeaponTagList(),
    private val onSelectionChanged: ((List<String>) -> Unit)? = null,
    private val selectedTagsSnapshot: List<String>? = null,
    private val unavailableTagsSnapshot: Set<String>? = null,
    private val persistenceContext: ShipEditorPersistenceContext? = null,
    useMomentaryVisualState: Boolean = false,
) :
    ButtonBase<String>(tag, button, false, useMomentaryVisualState) {

    private val visualState = CampaignToggleVisualState()

    companion object {
        var campaignTagSelectionVersion = 0
            private set

        fun notifyCampaignTagSelectionChanged() {
            campaignTagSelectionVersion++
        }

        fun repairAndPersistSelectedTags(
            ship: FleetMemberAPI,
            group: Int,
            runtimeShip: ShipAPI? = null,
            visibleTags: List<String> = Settings.getCurrentWeaponTagList(),
            loadedTagsOverride: List<String>? = null,
            persistenceContext: ShipEditorPersistenceContext? = null,
        ): MutableList<String> {
            return TagButtonSelectionStore.repairAndPersistSelectedTags(
                ship = ship,
                group = group,
                runtimeShip = runtimeShip,
                visibleTags = visibleTags,
                loadedTagsOverride = loadedTagsOverride,
                persistenceContext = persistenceContext,
            )
        }

        fun selectedTagsAllowedForDisplay(
            ship: FleetMemberAPI,
            group: Int,
            visibleTags: List<String> = Settings.getCurrentWeaponTagList(),
            loadedTags: List<String>,
        ): MutableList<String> {
            return TagButtonSelectionStore.selectedTagsAllowedForDisplay(ship, group, visibleTags, loadedTags)
        }

        fun unavailableTagsForSelection(
            ship: FleetMemberAPI,
            group: Int,
            candidateTags: List<String>,
            currentTags: List<String>,
        ): Set<String> {
            return TagButtonAvailability.unavailableTagsForSelection(ship, group, candidateTags, currentTags)
        }

        fun createLegacyTooltipTagButtonGroup(
            ship: FleetMemberAPI,
            group: Int,
            tooltip: TooltipMakerAPI,
            tagView: TagListView
        ): List<TagButton> {
            return TagButtonFactory.createLegacyTooltipTagButtonGroup(ship, group, tooltip, tagView)
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
            return TagButtonFactory.createCampaignTagButtonGroup(
                ship = ship,
                group = group,
                panel = panel,
                renderedTags = renderedTags,
                pinned = pinned,
                runtimeShip = runtimeShip,
                visibleTagsForSanitization = visibleTagsForSanitization,
                onSelectionChanged = onSelectionChanged,
                sanitizedTagsOverride = sanitizedTagsOverride,
                unavailableTagsOverride = unavailableTagsOverride,
                persistenceContext = persistenceContext,
            )
        }
    }

    private fun loadCurrentTags(): List<String> =
        TagButtonSelectionStore.loadTagsForContext(ship, group, runtimeShip, persistenceContext)

    private fun saveCurrentTags(tags: List<String>) =
        TagButtonSelectionStore.saveTagsForContext(ship, group, runtimeShip, tags, persistenceContext)

    private fun currentTagsForClick(): MutableList<String> =
        selectedTagsSnapshot?.toMutableList()
            ?: TagButtonSelectionStore.selectedTagsAllowedForDisplay(
                ship = ship,
                group = group,
                visibleTags = visibleTagsForSanitization,
                loadedTags = loadCurrentTags(),
            )

    private fun isUnavailableForClick(tags: List<String>): Boolean =
        if (unavailableTagsSnapshot != null) {
            associatedValue in unavailableTagsSnapshot
        } else {
            TagButtonAvailability.isTagUnavailable(ship, group, associatedValue, tags)
        }

    fun isEditableTag(): Boolean = EditableWeaponTagDefinitions.isEditable(associatedValue)

    internal fun setCheckedFromPersistence(checked: Boolean) {
        setCheckedFromPersistenceVisual(checked, visualState)
    }

    private fun applyToggleableVisualState(force: Boolean = false) {
        visualState.apply(button, force)
    }

    private fun setUnavailableState() {
        disableAsUnavailable(visualState)
    }

    private fun rejectUnavailableClick() {
        setUnavailableState()
    }

    private fun afterPersistedSelectionChanged(tags: List<String>) {
        if (onSelectionChanged == null) {
            updateDisabledButtons(tags)
            notifyCampaignTagSelectionChanged()
        } else {
            onSelectionChanged.invoke(tags)
        }
    }

    internal fun updateDisabledButtons(tags: List<String>) {
        if (TagButtonAvailability.isTagUnavailable(ship, group, associatedValue, tags)) {
            // TODO: If Starsector exposes a stable disabled-click sound API, trigger it when unavailable tags are clicked.
            setUnavailableState()
        } else {
            setAvailableState()
        }
        sameGroupButtons.forEach {
            val tagButton = it as? TagButton ?: return@forEach
            if (TagButtonAvailability.isTagUnavailable(ship, group, it.associatedValue, tags)) {
                tagButton.setUnavailableState()
            } else {
                tagButton.setAvailableState()
            }
        }
    }

    private fun setAvailableState() {
        setAvailableForConfiguredVisualState(visualState)
    }

    override fun executeCallbackIfChecked(): Boolean {
        if (useMomentaryVisualState) {
            return executeMomentaryVisualCallbackIfClicked()
        }
        if (!active && button.isChecked) {
            val tags = currentTagsForClick()
            if (!button.isEnabled || isUnavailableForClick(tags)) {
                rejectUnavailableClick()
                return false
            }
            val updatedTags = (tags + associatedValue).distinct()
            saveCurrentTags(updatedTags)
            setActiveChecked(true)
            afterPersistedSelectionChanged(updatedTags)
            syncButtonCheckedToActive()
            applyToggleableVisualState()
            return true
        } else if (active && !button.isChecked) {
            val tags = (selectedTagsSnapshot ?: loadCurrentTags()).filterNot { it == associatedValue }
            saveCurrentTags(tags)
            uncheck()
            afterPersistedSelectionChanged(tags)
            syncButtonCheckedToActive()
            applyToggleableVisualState()
            return true
        }
        syncButtonCheckedToActive()
        applyToggleableVisualState()
        return false
    }

    private fun executeMomentaryVisualCallbackIfClicked(): Boolean {
        return executeMomentaryVisualToggleIfClicked(
            currentState = { currentTagsForClick().toList() },
            isUnavailable = ::isUnavailableForClick,
            rejectUnavailable = ::rejectUnavailableClick,
            updatedState = { currentlyActive, tags ->
                if (currentlyActive) {
                    (selectedTagsSnapshot ?: loadCurrentTags()).filterNot { it == associatedValue }
                } else {
                    (tags + associatedValue).distinct()
                }
            },
            saveState = ::saveCurrentTags,
            afterStateChanged = ::afterPersistedSelectionChanged,
        )
    }

    override fun onActivate() {
    }
}
