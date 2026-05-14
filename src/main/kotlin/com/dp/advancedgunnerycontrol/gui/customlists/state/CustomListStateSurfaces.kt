package com.dp.advancedgunnerycontrol.gui.customlists.state

internal interface CustomListManagerState {
    fun normalizeStagedState(currentTags: List<String>): CustomTagManagerStagedState
    fun normalizeShipModeStagedState(currentModes: List<String>): CustomShipModeManagerStagedState
    fun shipModeAdditions(): MutableList<String>
    fun shipModeRemovals(): MutableSet<String>
    fun shipModeEdits(): Map<String, String>
    fun expandedCategories(): MutableSet<String>
    fun expandedArchetypes(): MutableSet<String>
    fun expandedListSections(): MutableSet<String>
    fun toggleManagerCategory(categoryTitle: String)
    fun toggleManagerArchetype(archetypeId: String)
    fun toggleListSection(sectionId: String)
    fun sourceForEdit(editedTag: String): String?
    fun toggleMarkedForRemoval(tag: String)
    fun removeAddition(tag: String)
    fun stageAddition(tag: String, currentTags: List<String>)
    fun sourceForShipModeEdit(editedMode: String): String?
    fun toggleShipModeMarkedForRemoval(mode: String)
    fun removeShipModeAddition(mode: String)
    fun stageShipModeAddition(mode: String, currentModes: List<String>)
}

internal interface CustomListChangeReviewState {
    fun normalizeStagedState(currentTags: List<String>): CustomTagManagerStagedState
    fun normalizeShipModeStagedState(currentModes: List<String>): CustomShipModeManagerStagedState
    fun changeReviewExpandedSections(defaultSections: Collection<String>): MutableSet<String>
    fun toggleChangeReviewSection(sectionId: String, defaultSections: Collection<String>)
}
