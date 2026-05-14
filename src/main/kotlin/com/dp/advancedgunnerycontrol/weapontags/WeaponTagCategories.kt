package com.dp.advancedgunnerycontrol.weapontags

data class WeaponTagCategory(
    val title: String,
    private val orderedTemplates: List<String>,
) {
    private val orderByTemplate = orderedTemplates.withIndex().associate { it.value to it.index }

    private class CategoryTagComparator(
        private val orderByTemplate: Map<String, Int>,
        private val originalOrder: Map<String, Int>
    ) : Comparator<String> {
        override fun compare(left: String, right: String): Int {
            val leftOrder = orderByTemplate[tagNameToRegexName(left)] ?: Int.MAX_VALUE
            val rightOrder = orderByTemplate[tagNameToRegexName(right)] ?: Int.MAX_VALUE
            val templateComparison = leftOrder.compareTo(rightOrder)
            if (templateComparison != 0) return templateComparison
            return (originalOrder[left] ?: Int.MAX_VALUE).compareTo(originalOrder[right] ?: Int.MAX_VALUE)
        }
    }

    fun contains(tag: String): Boolean = tagNameToRegexName(tag) in orderByTemplate

    fun sortTags(tags: List<String>): List<String> {
        val originalOrder = tags.withIndex().associate { it.value to it.index }
        return tags.sortedWith(CategoryTagComparator(orderByTemplate, originalOrder))
    }

    companion object {
        val CATEGORIES: List<WeaponTagCategory> = listOf(
            WeaponTagCategory(
                "Target",
                listOf(
                    "TargetShield",
                    "TargetShield+",
                    "TargetShield(S>N%)",
                    "TargetShield(TF>N%)",
                    "TargetShield(SF>N%)",
                    "TargetShield(HF>N%)",
                    "TargetPhase",
                    "TargetBig",
                    "TargetSmall",
                    "TargetFighter",
                    "TargetOverloaded",
                    "ShipTarget",
                    "Opportunist",
                    "Opportunist(A<N%)",
                )
            ),
            WeaponTagCategory(
                "Avoid",
                listOf(
                    "AvoidShield",
                    "AvoidShield+",
                    "AvoidShield(S<N%)",
                    "AvoidShield(TF>N%)",
                    "AvoidShield(SF>N%)",
                    "AvoidShield(HF>N%)",
                    "AvoidArmor",
                    "AvoidPhased",
                    "AvoidPD(Waste>N%)",
                    "AvoidPD(H<N)",
                    "AvoidDebris",
                )
            ),
            WeaponTagCategory(
                "No",
                listOf(
                    "NoShield",
                    "NoMissile",
                    "NoFighter",
                    "NoPD",
                    "DoNotShoot",
                )
            ),
            WeaponTagCategory(
                "Force",
                listOf(
                    "ForceAutoFire",
                    "Force(TF<N%)",
                    "Force(SF<N%)",
                    "Force(HF<N%)",
                )
            ),
            WeaponTagCategory(
                "Hold",
                listOf(
                    "HoldFire(TF>N%)",
                    "HoldFire(SF>N%)",
                    "HoldFire(HF>N%)",
                )
            ),
            WeaponTagCategory(
                "PD",
                listOf(
                    "PD",
                    "PD(TF>N%)",
                    "PD(SF>N%)",
                    "PD(HF>N%)",
                    "PD(A<N%)",
                )
            ),
            WeaponTagCategory(
                "Priority",
                listOf(
                    "PrioFighter",
                    "PrioMissile",
                    "PrioSmall",
                    "PrioBig",
                    "PrioShip",
                    "PrioWounded",
                    "PrioWoundedPD",
                    "PrioHealthy",
                    "PrioShields",
                    "PrioHull",
                    "PrioFocused",
                    "PrioClose",
                    "PrioFar",
                    "PrioDense",
                )
            ),
            WeaponTagCategory(
                "Synchronised",
                listOf(
                    "Ambush",
                    "SyncWindow",
                    "SyncVolley",
                )
            ),
        )

        val MISCELLANEOUS = WeaponTagCategory(
            "Miscellaneous",
            listOf(
                "CustomAI",
                "Merge",
                "DisableTags",
                "Range",
                "LowRoF(N%)",
                "Panic",
                "BlockBeams",
            )
        )

        val DISPLAY_CATEGORIES: List<WeaponTagCategory> = CATEGORIES + MISCELLANEOUS

        fun categoryFor(tag: String): WeaponTagCategory {
            return CATEGORIES.firstOrNull { it.contains(tag) } ?: MISCELLANEOUS
        }
    }
}
