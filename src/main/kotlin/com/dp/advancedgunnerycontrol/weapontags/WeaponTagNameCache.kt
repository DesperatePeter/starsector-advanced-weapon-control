package com.dp.advancedgunnerycontrol.weapontags

private data class WeaponTagPairKey(val candidate: String, val existing: String)

internal object WeaponTagNameCache {
    private val canonicalByRaw = mutableMapOf<String, String>()
    private val templateByRaw = mutableMapOf<String, String>()
    private val tooltipByRaw = mutableMapOf<String, String>()
    private val incompatibleByRawPair = mutableMapOf<WeaponTagPairKey, Boolean>()

    fun canonical(raw: String, build: () -> String): String =
        canonicalByRaw.getOrPut(raw, build)

    fun template(raw: String, build: () -> String): String =
        templateByRaw.getOrPut(raw, build)

    fun tooltip(raw: String, build: () -> String): String =
        tooltipByRaw.getOrPut(raw, build)

    fun incompatiblePair(candidate: String, existing: String, build: () -> Boolean): Boolean =
        incompatibleByRawPair.getOrPut(WeaponTagPairKey(candidate, existing), build)

    fun clear() {
        canonicalByRaw.clear()
        templateByRaw.clear()
        tooltipByRaw.clear()
        incompatibleByRawPair.clear()
    }
}
