package com.dp.advancedgunnerycontrol.gui.suggestedtags.view


import com.dp.advancedgunnerycontrol.gui.suggestedtags.filters.WeaponFilter
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class WeaponListView(private val viewSize: Int) {
    private val weaponIds
        get() = filteredWeaponIds()
    private var index = 0
        private set
    private var lastIndex = 0
    private val sortedWeaponIds by lazy {
        Global.getSector()?.allWeaponIds
            .orEmpty()
            .filter(::isSuggestedTagVisibleWeaponId)
            .sortedWith(WEAPON_NAME_COMPARATOR)
    }
    private var cachedFilters: List<WeaponFilter>? = null
    private var cachedWeaponIds: List<String>? = null
    private val maxIndex
        get() = max(0, weaponIds.size - viewSize)
    private val pageCount: Int
        get() = max(1, ceil(weaponIds.size.toFloat() / viewSize.toFloat()).toInt())

    private var filters = mutableListOf<WeaponFilter>()

    val pageString: String
        get() {
            clampIndex()
            val pageNumber = if (index == maxIndex && maxIndex > 0) pageCount else index / viewSize + 1
            return "Page $pageNumber/$pageCount"
        }

    companion object{
        private val WEAPON_NAME_COMPARATOR = Comparator<String> { left, right ->
            nameFromId(left).compareTo(nameFromId(right))
        }

        fun nameFromId(name: String): String = WeaponFilter.specForOrNull(name)?.weaponName ?: "Unknown"

        private fun isSuggestedTagVisibleWeaponId(weaponId: String): Boolean {
            if (nameFromId(weaponId).trim().firstOrNull()?.isAsciiLetter() != true) return false
            return WeaponFilter.specForOrNull(weaponId)?.let(::isSuggestedTagVisibleWeaponSpec) == true
        }

        private fun isSuggestedTagVisibleWeaponSpec(weaponSpec: WeaponSpecAPI): Boolean {
            if (weaponSpec.type == WeaponAPI.WeaponType.DECORATIVE) return false
            if (weaponSpec.mountType == WeaponAPI.WeaponType.DECORATIVE) return false
            val role = weaponSpec.primaryRoleStr?.trim().orEmpty()
            if (role.equals("DECORATIVE", ignoreCase = true)) return false
            return true
        }

        private fun Char.isAsciiLetter(): Boolean {
            return this in 'A'..'Z' || this in 'a'..'z'
        }

    }

    fun toggleFilter(filter: WeaponFilter){
        if(filters.contains(filter)){
            filters.remove(filter)
        }else{
            filters.add(filter)
        }
        clampIndex()
    }

    fun containsFilter(filter: WeaponFilter): Boolean{
        return filters.contains(filter)
    }

    fun activeFilters(): List<WeaponFilter> {
        val active = filters.toSet()
        return WeaponFilter.allFilters.filter { it in active }
    }

    fun clearFilters(){
        filters.clear()
        clampIndex()
    }

    fun replaceFilters(nextFilters: List<WeaponFilter>) {
        filters.clear()
        WeaponFilter.allFilters.forEach { filter ->
            if (filter in nextFilters) filters.add(filter)
        }
        clampIndex()
    }

    private fun matchesFilters(weaponSpec: WeaponSpecAPI, groupedFilters: Map<WeaponFilter.FilterType, List<WeaponFilter>>): Boolean {
        groupedFilters.values.forEach { filtersForType ->
            var matchedType = false
            filtersForType.forEach { filter ->
                if (filter.matches(weaponSpec)) matchedType = true
            }
            if (!matchedType) {
                return false
            }
        }
        return true
    }

    private fun filteredWeaponIds(): List<String> {
        val filterSnapshot = filters.toList()
        val cached = cachedWeaponIds
        if (cached != null && cachedFilters == filterSnapshot) {
            return cached
        }
        val groupedFilters = filtersByType(filterSnapshot)
        val filtered = if (groupedFilters.isEmpty()) {
            sortedWeaponIds
        } else {
            sortedWeaponIds.filter { weaponId ->
                WeaponFilter.specForOrNull(weaponId)?.let { spec ->
                    matchesFilters(spec, groupedFilters)
                } == true
            }
        }
        cachedFilters = filterSnapshot
        cachedWeaponIds = filtered
        return filtered
    }

    private fun filtersByType(filters: List<WeaponFilter>): Map<WeaponFilter.FilterType, List<WeaponFilter>> {
        val grouped = linkedMapOf<WeaponFilter.FilterType, MutableList<WeaponFilter>>()
        filters.forEach { filter ->
            val type = filter.type()
            var filtersForType = grouped[type]
            if (filtersForType == null) {
                filtersForType = mutableListOf()
                grouped[type] = filtersForType
            }
            filtersForType.add(filter)
        }
        return grouped
    }

    fun cycle(){
        clampIndex()
        if(index >= maxIndex) {
            if (maxIndex > 0) index = 0
            return
        }
        index+= viewSize
        if(index > maxIndex) index = maxIndex
    }
    fun cycleBackwards(){
        clampIndex()
        if(index == 0) {
            if (maxIndex > 0) index = maxIndex
            return
        }
        index-= viewSize
        if(index < 0){
            index = 0
        }
    }

    private fun clampIndex() {
        index = index.coerceIn(0, maxIndex)
    }

    fun currentIds(): List<String> {
        val ids = weaponIds
        clampIndex()
        if (ids.isEmpty()) return emptyList()
        return ids.subList(index, min(index + viewSize, ids.size))
    }
    fun currentNames(): List<String> = currentIds().map { nameFromId(it) }
    fun hasChanged(): Boolean{
        if(index != lastIndex){
            lastIndex = index
            return true
        }
        return false
    }

    fun markRendered() {
        lastIndex = index
    }
}
