package com.dp.advancedgunnerycontrol.weapontags

fun getTagTooltip(tag: String): String =
    WeaponTagNameCache.tooltip(tag) { getTagTooltipUncached(tag) }

private fun getTagTooltipUncached(tag: String): String {
    val canonicalTag = canonicalizeWeaponTagName(tag)
    baseTagTooltip(canonicalTag)?.let {
        return withEditableParameterSummary(canonicalTag, it)
    }
    return withEditableParameterSummary(canonicalTag, dynamicTagTooltip(canonicalTag))
}
