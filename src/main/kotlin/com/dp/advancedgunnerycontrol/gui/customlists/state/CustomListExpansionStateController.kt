package com.dp.advancedgunnerycontrol.gui.customlists.state

internal class CustomListExpansionStateController(
    private val draftStore: CustomListDraftStore,
) {
    fun expandedCategories(): MutableSet<String> =
        draftStore.valueList(CustomListDraftKeys.Tags.EXPANDED_CATEGORIES).toMutableSet()

    fun expandedArchetypes(): MutableSet<String> =
        draftStore.valueList(CustomListDraftKeys.Tags.EXPANDED_ARCHETYPES).toMutableSet()

    fun changeReviewExpandedSections(defaultSections: Collection<String>): MutableSet<String> {
        val saved = draftStore.value(CustomListDraftKeys.ChangeReview.EXPANDED_SECTIONS)
            ?.split(CustomListDraftKeys.STATE_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.toMutableSet()
        return saved ?: defaultSections.toMutableSet()
    }

    fun toggleManagerCategory(categoryTitle: String) {
        draftStore.toggleSet(CustomListDraftKeys.Tags.EXPANDED_CATEGORIES, categoryTitle)
    }

    fun toggleManagerArchetype(archetypeId: String) {
        draftStore.toggleSet(CustomListDraftKeys.Tags.EXPANDED_ARCHETYPES, archetypeId)
    }

    fun expandedListSections(): MutableSet<String> {
        val saved = draftStore.value(CustomListDraftKeys.ListSections.EXPANDED_MANAGER_SECTIONS)
            ?: return mutableSetOf(CustomListDraftKeys.ListSections.TAGS)
        return saved
            .split(CustomListDraftKeys.STATE_SEPARATOR)
            .filter { it.isNotBlank() }
            .toMutableSet()
    }

    fun toggleListSection(sectionId: String) {
        draftStore.toggleSet(CustomListDraftKeys.ListSections.EXPANDED_MANAGER_SECTIONS, sectionId)
    }

    fun toggleChangeReviewSection(sectionId: String, defaultSections: Collection<String>) {
        val expanded = changeReviewExpandedSections(defaultSections)
        if (!expanded.add(sectionId)) expanded.remove(sectionId)
        draftStore.writeValue(
            CustomListDraftKeys.ChangeReview.EXPANDED_SECTIONS,
            expanded.joinToString(CustomListDraftKeys.STATE_SEPARATOR),
        )
    }
}
