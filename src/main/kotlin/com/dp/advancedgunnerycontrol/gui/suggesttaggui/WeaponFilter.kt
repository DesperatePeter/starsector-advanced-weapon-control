package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI

abstract class WeaponFilter {
    abstract fun matches(weaponSpec: WeaponSpecAPI): Boolean
    abstract fun type(): FilterType
    abstract fun name(): String

    enum class FilterType{SIZE, WEAPON_TYPE}

    fun matches(weapon: String): Boolean{
        return matches(Global.getSettings().getWeaponSpec(weapon))
    }

    companion object{
        object ballisticsFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.type == WeaponAPI.WeaponType.BALLISTIC
            override fun type(): FilterType = FilterType.WEAPON_TYPE
            override fun name(): String = "Ballistics"

        }
        object energyFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.type == WeaponAPI.WeaponType.ENERGY
            override fun type(): FilterType = FilterType.WEAPON_TYPE
            override fun name(): String = "Energy"

        }
        object missileFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.type == WeaponAPI.WeaponType.MISSILE
            override fun type(): FilterType = FilterType.WEAPON_TYPE
            override fun name(): String = "Missiles"

        }
        object smallFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.SMALL
            override fun type(): FilterType = FilterType.SIZE
            override fun name(): String = "Small"
        }
        object mediumFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.MEDIUM
            override fun type(): FilterType = FilterType.SIZE
            override fun name(): String = "Medium"
        }
        object largeFilter : WeaponFilter() {
            override fun matches(weaponSpec: WeaponSpecAPI): Boolean = weaponSpec.size == WeaponAPI.WeaponSize.LARGE
            override fun type(): FilterType = FilterType.SIZE
            override fun name(): String = "Large"
        }
        public val allFilters = listOf(ballisticsFilter, energyFilter, missileFilter, smallFilter, mediumFilter, largeFilter)
    }
}