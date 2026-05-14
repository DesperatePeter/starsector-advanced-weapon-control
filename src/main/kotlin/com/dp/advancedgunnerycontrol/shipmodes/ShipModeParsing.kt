package com.dp.advancedgunnerycontrol.shipmodes

fun shipModeDisplayName(modeName: String): String {
    val parsed = parseShipMode(modeName) ?: return canonicalizeShipModeName(modeName)
    if (parsed.mode == ShipModes.PERSONALITY_OVERRIDE) {
        val personality = parsed.personality ?: return parsed.canonicalName
        return "Personality ($personality)"
    }
    return parsed.canonicalName
}

fun parseShipMode(mode: String): ParsedShipMode? {
    return ShipModeParser.parse(mode)
}

fun isSupportedShipModeName(mode: String): Boolean = parseShipMode(mode) != null

fun canonicalizeShipModeName(mode: String): String = parseShipMode(mode)?.canonicalName ?: mode

fun canonicalizeShipModeNames(modes: List<String>): List<String> {
    val canonicalModes = mutableListOf<String>()
    modes.map(::canonicalizeShipModeName).forEach { mode ->
        canonicalModes.removeAll { existing -> areShipModesMutuallyExclusive(mode, existing) }
        if (mode !in canonicalModes) {
            canonicalModes += mode
        }
    }
    return canonicalModes
}

fun areShipModesMutuallyExclusive(candidate: String, existing: String): Boolean {
    val candidateMode = parseShipMode(candidate)?.mode ?: return false
    val existingMode = parseShipMode(existing)?.mode ?: return false
    return candidateMode == ShipModes.PERSONALITY_OVERRIDE &&
        existingMode == ShipModes.PERSONALITY_OVERRIDE
}
