package com.dp.advancedgunnerycontrol.weapontags

enum class SyncSystemTriggerOption(
    val token: String,
    val label: String,
    val systemId: String,
) {
    ACCELERATED_AMMO_FEEDER("AAF", "Accelerated Ammo Feeder", "ammofeed"),
    HIGH_ENERGY_FOCUS("HEF", "High Energy Focus", "highenergyfocus"),
    LIDAR_ARRAY("LIDAR", "Lidar Array", "lidararray"),
    TEMPORAL_SHELL("TEMPORAL", "Temporal Shell", "temporalshell"),
    ENTROPY_AMPLIFIER("ENTROPY", "Entropy Amplifier", "entropyamplifier");

    companion object {
        fun fromToken(token: String): SyncSystemTriggerOption? {
            return values().firstOrNull { it.token.equals(token, ignoreCase = true) }
        }
    }
}

data class SyncTagOptions(
    val family: String,
    val requireShipTarget: Boolean = false,
    val systemTriggers: Set<SyncSystemTriggerOption> = emptySet(),
) {
    fun canonicalTag(): String {
        val tokens = mutableListOf<String>()
        if (requireShipTarget) tokens += SYNC_REQUIRE_TARGET_TOKEN
        syncTriggerOrder
            .filter { it in systemTriggers }
            .forEach { tokens += it.token }
        return if (tokens.isEmpty()) family else "$family(${tokens.joinToString(",")})"
    }

    fun displayName(): String {
        if (systemTriggers.isEmpty()) return canonicalTag()
        return when {
            requireShipTarget -> "*$family($SYNC_REQUIRE_TARGET_TOKEN)"
            else -> "*$family"
        }
    }
}

private const val SYNC_REQUIRE_TARGET_TOKEN = "X"

private val syncFamilies = setOf("SyncWindow", "SyncVolley", "Ambush")
val syncTriggerOrder = listOf(
    SyncSystemTriggerOption.ACCELERATED_AMMO_FEEDER,
    SyncSystemTriggerOption.HIGH_ENERGY_FOCUS,
    SyncSystemTriggerOption.LIDAR_ARRAY,
    SyncSystemTriggerOption.TEMPORAL_SHELL,
    SyncSystemTriggerOption.ENTROPY_AMPLIFIER,
)

private val syncTagOptionsRegex = Regex("(SyncWindow|SyncVolley|Ambush)(?:\\(([^)]*)\\))?")

fun parseSyncTagOptions(tag: String): SyncTagOptions? {
    val match = syncTagOptionsRegex.matchEntire(tag) ?: return null
    val family = match.groupValues[1].takeIf { it in syncFamilies } ?: return null
    val rawOptions = match.groupValues.getOrNull(2).orEmpty()
    if (rawOptions.isBlank()) return SyncTagOptions(family)

    var requireShipTarget = false
    val triggers = linkedSetOf<SyncSystemTriggerOption>()
    rawOptions
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { token ->
            if (token.equals(SYNC_REQUIRE_TARGET_TOKEN, ignoreCase = true)) {
                requireShipTarget = true
                return@forEach
            }
            val trigger = SyncSystemTriggerOption.fromToken(token) ?: return null
            triggers += trigger
        }
    return SyncTagOptions(family, requireShipTarget, triggers)
}

fun canonicalizeSyncTagOptions(tag: String): String? = parseSyncTagOptions(tag)?.canonicalTag()
