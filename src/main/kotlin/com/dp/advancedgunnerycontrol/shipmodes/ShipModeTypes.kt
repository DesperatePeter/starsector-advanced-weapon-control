package com.dp.advancedgunnerycontrol.shipmodes

enum class ShipModes {
    DEFAULT, FORCE_AUTOFIRE, PERSONALITY_OVERRIDE, PRE_AIM, SHIELDS_OFF, VENT, VENT_AGGRESSIVE,
    RETREAT, CR_RETREAT, HULL_RETREAT, NO_SYSTEM, SHIELDS_UP, SPAM_SYSTEM, CHARGE, SHIELDS_UP_PLUS,
    STAY_AWAY, FAR_AWAY, NEVER_VENT
}

const val defaultShipMode = "DEFAULT"

data class ParsedShipMode(
    val mode: ShipModes,
    val canonicalName: String,
    val personality: String? = null,
    val fluxThreshold: Float? = null,
    val hullThreshold: Float? = null,
    val crThresholds: List<Float?> = emptyList(),
    val hullThresholds: List<Float?> = emptyList(),
    val directRetreat: Boolean? = null,
    val ventSafetyFactor: Float? = null,
    val aggressiveVent: Boolean = false,
)