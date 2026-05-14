package com.dp.advancedgunnerycontrol.gui.suggestedtags.controls

import com.dp.advancedgunnerycontrol.gui.controls.buttons.ButtonBase
import com.dp.advancedgunnerycontrol.gui.controls.toggles.CampaignToggleVisualState
import com.dp.advancedgunnerycontrol.gui.session.TagListView
import com.dp.advancedgunnerycontrol.settings.Settings
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

/**
 * Visible suggested-tag row component.
 * Used in Customize Suggested Tags to toggle one tag for one weapon id.
 */
class SuggestedTagButton(
    private val weaponId: String,
    tag: String,
    button: ButtonAPI,
    private val selectedTagsSnapshot: List<String>? = null,
    private val unavailableTagsSnapshot: Set<String>? = null,
    private val onSelectionChanged: ((List<String>) -> Unit)? = null,
    useMomentaryVisualState: Boolean = false,
) : ButtonBase<String>(tag, button, false, useMomentaryVisualState) {
    private val visualState = CampaignToggleVisualState()

    companion object {
        var suggestedTagSelectionVersion = 0
            private set

        fun unavailableTagsForSelection(candidateTags: List<String>, currentTags: List<String>): Set<String> {
            return SuggestedTagAvailability.unavailableTagsForSelection(candidateTags, currentTags)
        }

        fun supportedSuggestedTagsForWeapon(
            weaponId: String,
            supportedTags: Set<String> = Settings.getCurrentWeaponTagList().toSet(),
        ): List<String> {
            return SuggestedTagSelectionStore.supportedSuggestedTagsForWeapon(weaponId, supportedTags)
        }

        fun createButtonGroup(
            weaponId: String,
            tooltip: TooltipMakerAPI,
            tagView: TagListView,
        ): List<SuggestedTagButton> {
            return SuggestedTagButtonFactory.createButtonGroup(weaponId, tooltip, tagView)
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
            return SuggestedTagButtonFactory.createCampaignButtonGroup(
                weaponId = weaponId,
                panel = panel,
                visibleTags = visibleTags,
                pinned = pinned,
                selectedTagsOverride = selectedTagsOverride,
                unavailableTagsOverride = unavailableTagsOverride,
                onSelectionChanged = onSelectionChanged,
            )
        }

        private fun notifySuggestedTagSelectionChanged() {
            suggestedTagSelectionVersion++
        }
    }

    fun isForWeapon(weaponId: String): Boolean = this.weaponId == weaponId

    internal fun setCheckedFromPersistence(checked: Boolean) {
        setCheckedFromPersistenceVisual(checked, visualState)
    }

    private fun applyToggleableVisualState(force: Boolean = false) {
        visualState.apply(button, force)
    }

    private fun setUnavailableState() {
        disableAsUnavailable(visualState)
    }

    private fun currentSuggestedTagsForWeapon(): List<String> {
        return supportedSuggestedTagsForWeapon(weaponId)
    }

    private fun currentSuggestedTagsForClick(): List<String> {
        return selectedTagsSnapshot ?: currentSuggestedTagsForWeapon()
    }

    private fun saveSuggestedTags(tags: List<String>) {
        SuggestedTagSelectionStore.saveSuggestedTags(weaponId, tags)
    }

    private fun isUnavailableForCurrentSelection(tags: List<String>): Boolean {
        return unavailableTagsSnapshot?.let { associatedValue in it }
            ?: SuggestedTagAvailability.isTagUnavailable(associatedValue, tags)
    }

    private fun rejectUnavailableClick() {
        setUnavailableState()
    }

    override fun onActivate() {
    }

    private fun afterSuggestedTagsChanged(tags: List<String>) {
        if (onSelectionChanged == null) {
            updateDisabledButtons(tags)
            notifySuggestedTagSelectionChanged()
        } else {
            onSelectionChanged.invoke(tags)
        }
    }

    override fun executeCallbackIfChecked(): Boolean {
        if (useMomentaryVisualState) {
            return executeMomentaryVisualCallbackIfClicked()
        }
        if (!active && button.isChecked) {
            val tags = currentSuggestedTagsForClick()
            if (!button.isEnabled || isUnavailableForCurrentSelection(tags)) {
                rejectUnavailableClick()
                return false
            }
            val updatedTags = (tags + associatedValue).distinct()
            saveSuggestedTags(updatedTags)
            setActiveChecked(true)
            afterSuggestedTagsChanged(updatedTags)
            syncButtonCheckedToActive()
            applyToggleableVisualState()
            return true
        } else if (active && !button.isChecked) {
            val updatedTags = currentSuggestedTagsForClick().filter { it != associatedValue }
            saveSuggestedTags(updatedTags)
            uncheck()
            afterSuggestedTagsChanged(updatedTags)
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
            currentState = ::currentSuggestedTagsForClick,
            isUnavailable = ::isUnavailableForCurrentSelection,
            rejectUnavailable = ::rejectUnavailableClick,
            updatedState = { currentlyActive, tags ->
                if (currentlyActive) {
                    tags.filter { it != associatedValue }
                } else {
                    (tags + associatedValue).distinct()
                }
            },
            saveState = ::saveSuggestedTags,
            afterStateChanged = ::afterSuggestedTagsChanged,
        )
    }

    private fun updateDisabledButtons() {
        updateDisabledButtons(currentSuggestedTagsForWeapon())
    }

    internal fun updateDisabledButtons(tags: List<String>) {
        if (SuggestedTagAvailability.isTagUnavailable(associatedValue, tags)) {
            setUnavailableState()
        } else {
            setAvailableState()
        }
        sameGroupButtons.forEach {
            val tagButton = it as? SuggestedTagButton ?: return@forEach
            if (SuggestedTagAvailability.isTagUnavailable(it.associatedValue, tags)) {
                tagButton.setUnavailableState()
            } else {
                tagButton.setAvailableState()
            }
        }
    }

    private fun setAvailableState() {
        setAvailableForConfiguredVisualState(visualState)
    }
}
