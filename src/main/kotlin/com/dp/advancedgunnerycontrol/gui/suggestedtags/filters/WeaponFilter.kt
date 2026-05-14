package com.dp.advancedgunnerycontrol.gui.suggestedtags.filters


import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.WeaponSpecAPI

abstract class WeaponFilter {
    abstract fun matches(weaponSpec: WeaponSpecAPI): Boolean
    abstract fun type(): FilterType
    abstract fun name(): String

    enum class FilterType { SIZE, MOUNT_TYPE, FIRE_FORM, RANGE, FLUX_DAMAGE, AMMO, EMP, POINT_DEFENSE, DAMAGE_TYPE }
    enum class FilterCategory(val title: String) {
        MOUNT("Mount"),
        SIZE("Size"),
        FIRE_FORM("Form"),
        DAMAGE_TYPE("Damage"),
        SPECIAL("Special"),
        FLUX_DAMAGE("Flux/Damage"),
        RANGE("Range"),
    }

    fun category(): FilterCategory {
        return when (type()) {
            FilterType.MOUNT_TYPE -> FilterCategory.MOUNT
            FilterType.SIZE -> FilterCategory.SIZE
            FilterType.FIRE_FORM -> FilterCategory.FIRE_FORM
            FilterType.DAMAGE_TYPE -> FilterCategory.DAMAGE_TYPE
            FilterType.AMMO, FilterType.EMP, FilterType.POINT_DEFENSE -> FilterCategory.SPECIAL
            FilterType.FLUX_DAMAGE -> FilterCategory.FLUX_DAMAGE
            FilterType.RANGE -> FilterCategory.RANGE
        }
    }

    fun matches(weapon: String): Boolean {
        return specForOrNull(weapon)?.let(::matches) == true
    }

    companion object {
        fun specFor(weapon: String): WeaponSpecAPI {
            return Global.getSettings().getWeaponSpec(weapon)
        }

        fun specForOrNull(weapon: String): WeaponSpecAPI? {
            return runCatching { specFor(weapon) }.getOrNull()
        }

        val allFilters: List<WeaponFilter> = WeaponFilterCatalog.allFilters

        val filterCategories = listOf(
            FilterCategory.MOUNT,
            FilterCategory.SIZE,
            FilterCategory.FIRE_FORM,
            FilterCategory.DAMAGE_TYPE,
            FilterCategory.SPECIAL,
            FilterCategory.FLUX_DAMAGE,
            FilterCategory.RANGE,
        )

        private val filtersByCategory: Map<FilterCategory, List<WeaponFilter>> = buildFiltersByCategory()

        fun filtersIn(category: FilterCategory): List<WeaponFilter> {
            return filtersByCategory[category].orEmpty()
        }

        private fun buildFiltersByCategory(): Map<FilterCategory, List<WeaponFilter>> {
            val grouped = linkedMapOf<FilterCategory, MutableList<WeaponFilter>>()
            allFilters.forEach { filter ->
                val category = filter.category()
                var categoryFilters = grouped[category]
                if (categoryFilters == null) {
                    categoryFilters = mutableListOf()
                    grouped[category] = categoryFilters
                }
                categoryFilters.add(filter)
            }
            return grouped
        }

        fun categoryLabel(category: FilterCategory, expanded: Boolean): String {
            return "${category.title} (${if (expanded) "-" else "+"})"
        }
    }
}
