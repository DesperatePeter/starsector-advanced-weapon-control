package com.dp.advancedgunnerycontrol.weapontags

internal object EditableWeaponTagThresholdDefinitions {
    fun definitions(): List<EditableWeaponTagDefinition> =
        ammoAndTargetingThresholdDefinitions() +
            simpleDamageExclusionDefinitions()

    private fun ammoAndTargetingThresholdDefinitions(): List<EditableWeaponTagDefinition> = listOf(
        opportunistAmmoDefinition(),
        opportunistTuningDefinition(),
        integerThresholdDefinition(
            id = "pd_ammo_threshold",
            family = "PD",
            representativeTemplateTag = "PD(A<N%)",
            acceptedTemplateTags = setOf("PD(A<N%)", "ConservePDAmmo", "CnsrvPDAmmo"),
            label = "Ammo below",
            prefix = "PD(A<",
            suffix = "%)",
            thresholdMinimum = 0,
            thresholdMaximum = 99,
            defaultThreshold = 80,
            exclusivityPrefix = "PD:A"
        ),
        noPdWasteDefinition(),
        integerThresholdDefinition(
            id = "no_pd_health_threshold",
            family = "AvoidPD",
            representativeTemplateTag = "AvoidPD(H<N)",
            acceptedTemplateTags = setOf("AvoidPD(H<N>)", "AvoidPD(H<N)", "NoPD(H<N>)", "NoPD(H<N)", "IgnoreMinorPD"),
            label = "Health below",
            prefix = "AvoidPD(H<",
            suffix = ")",
            thresholdMinimum = 1,
            thresholdMaximum = 9999,
            defaultThreshold = 145,
            exclusivityPrefix = "AvoidPD:H"
        ),
        integerThresholdDefinition(
            id = "avoid_armor_threshold",
            family = "AvoidArmor",
            representativeTemplateTag = "AvoidArmor(N%)",
            acceptedTemplateTags = setOf("AvoidArmor", "AvoidArmor(N%)"),
            label = "Armor effectiveness",
            prefix = "AvoidArmor(",
            suffix = "%)",
            thresholdMinimum = 0,
            thresholdMaximum = 100,
            defaultThreshold = 33,
            exclusivityPrefix = "AvoidArmor",
            includeDamageTypeExclusions = true,
        ),
        integerThresholdDefinition(
            id = "panic_hull_threshold",
            family = "Panic",
            representativeTemplateTag = "Panic(H<N%)",
            acceptedTemplateTags = setOf("Panic", "Panic(H<N%)"),
            label = "Hull below",
            prefix = "Panic(H<",
            suffix = "%)",
            thresholdMinimum = 0,
            thresholdMaximum = 99,
            defaultThreshold = 25,
            exclusivityPrefix = "Panic:H"
        ),
        integerThresholdDefinition(
            id = "range_threshold",
            family = "Range",
            representativeTemplateTag = "Range<N%",
            acceptedTemplateTags = setOf("Range", "Range<N%"),
            label = "Range limit",
            prefix = "Range<",
            suffix = "%",
            thresholdMinimum = 1,
            thresholdMaximum = 99,
            defaultThreshold = 60,
            exclusivityPrefix = "Range"
        ),
        integerThresholdDefinition(
            id = "low_rof_threshold",
            family = "LowRoF",
            representativeTemplateTag = "LowRoF(N%)",
            acceptedTemplateTags = setOf("LowRoF(N%)", "LowRoF(200%)"),
            label = "Rate-of-fire factor",
            prefix = "LowRoF(",
            suffix = "%)",
            thresholdMinimum = 1,
            thresholdMaximum = 999,
            defaultThreshold = 200,
            exclusivityPrefix = "LowRoF"
        )
    )
}
