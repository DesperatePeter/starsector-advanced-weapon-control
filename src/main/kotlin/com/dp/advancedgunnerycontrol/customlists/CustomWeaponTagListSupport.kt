package com.dp.advancedgunnerycontrol.customlists

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.weapontags.EditableWeaponTagDefinitions
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagName
import com.dp.advancedgunnerycontrol.weapontags.canonicalizeWeaponTagNames
import com.dp.advancedgunnerycontrol.weapontags.tagNameToRegexName

internal object CustomWeaponTagListSupport {
    private data class CustomTagSortKey(
        val listOrder: Int,
        val canonicalTag: String
    )

    private class CustomTagComparator(
        private val sortKeys: Map<String, CustomTagSortKey>
    ) : Comparator<String> {
        override fun compare(left: String, right: String): Int {
            val leftKey = sortKeys.getValue(left)
            val rightKey = sortKeys.getValue(right)
            val orderComparison = leftKey.listOrder.compareTo(rightKey.listOrder)
            if (orderComparison != 0) return orderComparison
            return leftKey.canonicalTag.compareTo(rightKey.canonicalTag)
        }
    }

    private data class CompleteTagMetadata(
        val completeTags: List<String>,
        val templateOrder: Map<String, Int>,
        val supportSet: Set<String>
    )

    private var completeTagMetadata: CompleteTagMetadata? = null

    fun defaultCustomTags(): MutableList<String> =
        sortCustomTags(Settings.getWeaponTagListForMode(WeaponTagListMode.CLASSIC)).toMutableList()

    fun isSupportedTag(tag: String): Boolean {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        val templateTag = tagNameToRegexName(canonicalTag)
        val supportedTags = completeTagMetadata().supportSet
        return canonicalTag in supportedTags || templateTag in supportedTags
    }

    fun supportedCustomTagsForDisplay(state: CustomWeaponTagListState): List<String> {
        val supportedTags = completeTagMetadata().supportSet
        return sortCustomTags(state.customTags)
            .filter { tag ->
                val canonicalTag = canonicalizeWeaponTagName(tag)
                canonicalTag in supportedTags || tagNameToRegexName(canonicalTag) in supportedTags
            }
    }

    fun sortCustomTags(tags: List<String>): List<String> {
        val canonicalTags = canonicalizeWeaponTagNames(tags)
        val orderByTag = completeTemplateOrder()
        val sortKeys = canonicalTags.associateWith { sortKeyForTag(it, orderByTag) }
        return canonicalTags.sortedWith(CustomTagComparator(sortKeys))
    }

    private fun completeTemplateOrder(): Map<String, Int> {
        return completeTagMetadata().templateOrder
    }

    private fun completeTagMetadata(): CompleteTagMetadata {
        val completeTags = Settings.getWeaponTagListForMode(WeaponTagListMode.COMPLETE)
        val cached = completeTagMetadata
        if (cached != null && cached.completeTags == completeTags) return cached

        val order = linkedMapOf<String, Int>()
        val support = mutableSetOf<String>()
        completeTags.forEachIndexed { index, tag ->
            val canonicalTag = canonicalizeWeaponTagName(tag)
            order.putIfAbsent(canonicalTag, index)
            val templateTag = tagNameToRegexName(canonicalTag)
            order.putIfAbsent(templateTag, index)
            support += canonicalTag
            support += templateTag
        }
        return CompleteTagMetadata(
            completeTags = completeTags,
            templateOrder = order,
            supportSet = support
        ).also { completeTagMetadata = it }
    }

    private fun sortKeyForTag(tag: String, completeOrder: Map<String, Int>): CustomTagSortKey {
        val canonicalTag = canonicalizeWeaponTagName(tag)
        val templateTag = EditableWeaponTagDefinitions.parse(canonicalTag)?.templateTag
            ?: tagNameToRegexName(canonicalTag)
        return CustomTagSortKey(
            listOrder = completeOrder[canonicalTag]
                ?: completeOrder[templateTag]
                ?: Int.MAX_VALUE,
            canonicalTag = canonicalTag
        )
    }
}
