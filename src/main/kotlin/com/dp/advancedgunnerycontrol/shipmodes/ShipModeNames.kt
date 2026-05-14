package com.dp.advancedgunnerycontrol.shipmodes

private val canonicalShipModeFromString = mapOf(
    "DEFAULT" to ShipModes.DEFAULT,
    "ForceAutoFire" to ShipModes.FORCE_AUTOFIRE,
    "Personality(Steady)" to ShipModes.PERSONALITY_OVERRIDE,
    "PreAim" to ShipModes.PRE_AIM,
    "LowShield(TF>50%)" to ShipModes.SHIELDS_OFF,
    "Vent(TF>75%,S=2.0)" to ShipModes.VENT,
    "Vent(TF>25%,S=0.25)" to ShipModes.VENT,
    "Run(HP<50%)" to ShipModes.RETREAT,
    "RunCR(CR<50%)" to ShipModes.CR_RETREAT,
    "RunHP(HP<50%)" to ShipModes.HULL_RETREAT,
    "DisableSystem" to ShipModes.NO_SYSTEM,
    "ShieldUp(TF<90%)" to ShipModes.SHIELDS_UP,
    "SpamSystem" to ShipModes.SPAM_SYSTEM,
    "Charge" to ShipModes.CHARGE,
    "ShieldUp+" to ShipModes.SHIELDS_UP_PLUS,
    "StayAway" to ShipModes.STAY_AWAY,
    "FarAway" to ShipModes.FAR_AWAY,
    "NeverVent" to ShipModes.NEVER_VENT,
)

private val legacyShipModeAliases = mapOf(
    "ForceAF" to ShipModes.FORCE_AUTOFIRE,
    "LowShield" to ShipModes.SHIELDS_OFF,
    "LowShields" to ShipModes.SHIELDS_OFF,
    "Vent(TF>75%)" to ShipModes.VENT,
    "Vent(Flx>75%)" to ShipModes.VENT,
    "Vent(Flux>75%)" to ShipModes.VENT,
    "VentA(TF>25%)" to ShipModes.VENT_AGGRESSIVE,
    "VntA(Flx>25%)" to ShipModes.VENT_AGGRESSIVE,
    "VntA(Flux>25%)" to ShipModes.VENT_AGGRESSIVE,
    "RunCR(CR<75/50/25%)" to ShipModes.CR_RETREAT,
    "RunHP(HP<75/50/25%)" to ShipModes.HULL_RETREAT,
    "NoSystem" to ShipModes.NO_SYSTEM,
    "ShieldUp" to ShipModes.SHIELDS_UP,
    "ShieldsUp" to ShipModes.SHIELDS_UP,
    "ShieldsUp+" to ShipModes.SHIELDS_UP_PLUS
)

val shipModeFromString = canonicalShipModeFromString + legacyShipModeAliases

val shipModeToString = mapOf(
    ShipModes.DEFAULT to "DEFAULT",
    ShipModes.FORCE_AUTOFIRE to "ForceAutoFire",
    ShipModes.PERSONALITY_OVERRIDE to "Personality(Steady)",
    ShipModes.PRE_AIM to "PreAim",
    ShipModes.SHIELDS_OFF to "LowShield(TF>50%)",
    ShipModes.VENT to "Vent(TF>75%,S=2.0)",
    ShipModes.VENT_AGGRESSIVE to "Vent(TF>25%,S=0.25)",
    ShipModes.RETREAT to "Run(HP<50%)",
    ShipModes.CR_RETREAT to "RunCR(CR<50%)",
    ShipModes.HULL_RETREAT to "RunHP(HP<50%)",
    ShipModes.NO_SYSTEM to "DisableSystem",
    ShipModes.SHIELDS_UP to "ShieldUp(TF<90%)",
    ShipModes.SPAM_SYSTEM to "SpamSystem",
    ShipModes.CHARGE to "Charge",
    ShipModes.SHIELDS_UP_PLUS to "ShieldUp+",
    ShipModes.STAY_AWAY to "StayAway",
    ShipModes.FAR_AWAY to "FarAway",
    ShipModes.NEVER_VENT to "NeverVent",
)
