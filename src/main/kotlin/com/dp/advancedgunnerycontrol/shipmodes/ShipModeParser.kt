package com.dp.advancedgunnerycontrol.shipmodes

import com.dp.advancedgunnerycontrol.settings.Settings

internal object ShipModeParser {
    fun parse(mode: String): ParsedShipMode? {
        return parsedPersonality(mode) ?: parsedVent(mode) ?: parsedLegacyAggressiveVent(mode) ?: parsedPercent(
            mode = mode,
            regex = retreatModeRegex,
            shipMode = ShipModes.RETREAT,
            canonicalBuilder = { "Run(HP<$it%)" },
            thresholdSelector = { ParsedShipMode(ShipModes.RETREAT, "", hullThreshold = it) },
        ) ?: parsedSingleThresholdRetreat(
            mode = mode,
            regex = singleCrRetreatModeRegex,
            shipMode = ShipModes.CR_RETREAT,
            metric = "CR",
            thresholdsSetter = { slots, canonicalName ->
                ParsedShipMode(ShipModes.CR_RETREAT, canonicalName, crThresholds = slots)
            },
        ) ?: parsedMultiThresholdRetreat(
            mode = mode,
            regex = crRetreatModeRegex,
            shipMode = ShipModes.CR_RETREAT,
            canonicalPrefix = "RunCR(CR<",
            thresholdsSetter = { slots, canonicalName ->
                ParsedShipMode(ShipModes.CR_RETREAT, canonicalName, crThresholds = slots)
            },
        ) ?: parsedSingleThresholdRetreat(
            mode = mode,
            regex = singleHullRetreatModeRegex,
            shipMode = ShipModes.HULL_RETREAT,
            metric = "HP",
            thresholdsSetter = { slots, canonicalName ->
                ParsedShipMode(ShipModes.HULL_RETREAT, canonicalName, hullThresholds = slots)
            },
        ) ?: parsedMultiThresholdRetreat(
            mode = mode,
            regex = hullRetreatModeRegex,
            shipMode = ShipModes.HULL_RETREAT,
            canonicalPrefix = "RunHP(HP<",
            thresholdsSetter = { slots, canonicalName ->
                ParsedShipMode(ShipModes.HULL_RETREAT, canonicalName, hullThresholds = slots)
            },
        ) ?: parsedPercent(
            mode = mode,
            regex = lowShieldModeRegex,
            shipMode = ShipModes.SHIELDS_OFF,
            canonicalBuilder = { "LowShield(TF>$it%)" },
            thresholdSelector = { ParsedShipMode(ShipModes.SHIELDS_OFF, "", fluxThreshold = it) },
        ) ?: parsedPercent(
            mode = mode,
            regex = shieldUpModeRegex,
            shipMode = ShipModes.SHIELDS_UP,
            canonicalBuilder = { "ShieldUp(TF<$it%)" },
            thresholdSelector = { ParsedShipMode(ShipModes.SHIELDS_UP, "", fluxThreshold = it) },
        ) ?: parsedPercent(
            mode = mode,
            regex = shieldUpPlusModeRegex,
            shipMode = ShipModes.SHIELDS_UP_PLUS,
            canonicalBuilder = { "ShieldUp+(TF<$it%)" },
            thresholdSelector = { ParsedShipMode(ShipModes.SHIELDS_UP_PLUS, "", fluxThreshold = it) },
        ) ?: parsedLegacyNamedMode(mode) ?: parsedExactMode(mode)
    }

    private fun canonicalVentName(
        thresholdPercent: Int,
        safetyFactor: Float,
        aggressive: Boolean,
    ): String {
        val aggressiveSuffix = if (aggressive) ",A=T" else ""
        return "Vent(TF>$thresholdPercent%,S=${formatVentSafetyFactor(safetyFactor)}$aggressiveSuffix)"
    }

    private fun parsedVent(mode: String): ParsedShipMode? {
        val match = ventModeRegex.matchEntire(mode) ?: return null
        val percent = match.groupValues.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 100) ?: return null
        val aggressive = match.groupValues.getOrNull(3) == "T"
        val defaultSafety = if (aggressive) Settings.aggressiveVentSafetyFactor() else Settings.ventSafetyFactor()
        val safetyFactor = match.groupValues.getOrNull(2)
            ?.takeIf { it.isNotBlank() }
            ?.toFloatOrNull()
            ?.coerceIn(0.1f, 10f)
            ?: defaultSafety.coerceIn(0.1f, 10f)
        return ParsedShipMode(
            mode = ShipModes.VENT,
            canonicalName = canonicalVentName(percent, safetyFactor, aggressive),
            fluxThreshold = percent / 100f,
            ventSafetyFactor = safetyFactor,
            aggressiveVent = aggressive,
        )
    }

    private fun parsedLegacyAggressiveVent(mode: String): ParsedShipMode? {
        val percent = aggressiveVentModeRegex.matchEntire(mode)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: compactAggressiveVentModeRegex.matchEntire(mode)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: return null
        val thresholdPercent = percent.coerceIn(0, 100)
        val safetyFactor = Settings.aggressiveVentSafetyFactor().coerceIn(0.1f, 10f)
        return ParsedShipMode(
            mode = ShipModes.VENT,
            canonicalName = canonicalVentName(thresholdPercent, safetyFactor, aggressive = true),
            fluxThreshold = thresholdPercent / 100f,
            ventSafetyFactor = safetyFactor,
            aggressiveVent = true,
        )
    }

    private fun parsedPercent(
        mode: String,
        regex: Regex,
        shipMode: ShipModes,
        canonicalBuilder: (Int) -> String,
        thresholdSelector: (Float) -> ParsedShipMode,
    ): ParsedShipMode? {
        val percent = regex.matchEntire(mode)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?.coerceIn(0, 100)
            ?: return null
        return thresholdSelector(percent / 100f).copy(
            mode = shipMode,
            canonicalName = canonicalBuilder(percent),
        )
    }

    private fun parsedMultiThresholdRetreat(
        mode: String,
        regex: Regex,
        shipMode: ShipModes,
        canonicalPrefix: String,
        thresholdsSetter: (List<Float?>, String) -> ParsedShipMode,
    ): ParsedShipMode? {
        val match = regex.matchEntire(mode) ?: return null
        val rawThresholds = match.groupValues.getOrNull(1) ?: return null
        val slots = rawThresholds.split("/").takeIf { it.size == 3 }
            ?.map { slot ->
                if (slot == "-") null else slot.toIntOrNull()?.coerceIn(0, 100)?.div(100f)
            }
            ?: return null
        if (slots.all { it == null }) return null
        val directRetreat = directRetreatValue(match.groupValues.getOrNull(2))
        val canonicalSlots = slots.joinToString("/") { threshold ->
            threshold?.let { (it * 100f).toInt().toString() } ?: "-"
        }
        val directRetreatSuffix = if (directRetreat == true) ",DR=T" else ""
        return thresholdsSetter(slots, "$canonicalPrefix$canonicalSlots%$directRetreatSuffix)")
            .copy(mode = shipMode, directRetreat = directRetreat)
    }

    private fun parsedSingleThresholdRetreat(
        mode: String,
        regex: Regex,
        shipMode: ShipModes,
        metric: String,
        thresholdsSetter: (List<Float?>, String) -> ParsedShipMode,
    ): ParsedShipMode? {
        val match = regex.matchEntire(mode) ?: return null
        val threshold = match.groupValues.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 100)?.div(100f)
            ?: return null
        val directRetreat = directRetreatValue(match.groupValues.getOrNull(2))
        val directRetreatSuffix = if (directRetreat == true) ",DR=T" else ""
        val percent = (threshold * 100f).toInt()
        return thresholdsSetter(listOf(threshold), "Run$metric($metric<$percent%$directRetreatSuffix)")
            .copy(mode = shipMode, directRetreat = directRetreat)
    }

    private fun parsedPersonality(mode: String): ParsedShipMode? {
        val personality = personalityModeRegex.matchEntire(mode)?.groupValues?.getOrNull(1)
            ?.let(::shipModePersonalityLabel)
            ?: return null
        return ParsedShipMode(
            mode = ShipModes.PERSONALITY_OVERRIDE,
            canonicalName = "Personality($personality)",
            personality = personality,
        )
    }

    private fun parsedLegacyNamedMode(mode: String): ParsedShipMode? {
        return when (mode) {
            "LowShield", "LowShields" -> {
                val threshold = Settings.shieldOffThreshold().coerceIn(0f, 1f)
                ParsedShipMode(
                    mode = ShipModes.SHIELDS_OFF,
                    canonicalName = "LowShield(TF>${(threshold * 100f).toInt()}%)",
                    fluxThreshold = threshold,
                )
            }
            "ShieldUp", "ShieldsUp" -> ParsedShipMode(
                mode = ShipModes.SHIELDS_UP,
                canonicalName = "ShieldUp(TF<90%)",
                fluxThreshold = 0.9f,
            )
            "ShieldUp+", "ShieldsUp+" -> ParsedShipMode(
                mode = ShipModes.SHIELDS_UP_PLUS,
                canonicalName = "ShieldUp+(TF<100%)",
                fluxThreshold = 1f,
            )
            else -> null
        }
    }

    private fun parsedExactMode(mode: String): ParsedShipMode? {
        return shipModeFromString[mode]?.let { exactMode ->
            ParsedShipMode(
                mode = exactMode,
                canonicalName = shipModeToString[exactMode] ?: mode,
            )
        }
    }

    private fun directRetreatValue(value: String?): Boolean? {
        return when (value) {
            "T" -> true
            "F" -> false
            else -> null
        }
    }
}
