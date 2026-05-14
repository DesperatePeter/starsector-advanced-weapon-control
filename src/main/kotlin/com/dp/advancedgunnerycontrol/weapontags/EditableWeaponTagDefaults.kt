package com.dp.advancedgunnerycontrol.weapontags

import com.dp.advancedgunnerycontrol.settings.Settings
import java.util.Locale
import kotlin.math.roundToInt

internal fun settingsPercent(value: Float, min: Int = 0, max: Int = 100): Int =
    (value * 100f).roundToInt().coerceIn(min, max)

internal fun formatDecimalTagNumber(value: Float): String {
    var rendered = String.format(Locale.US, "%.2f", value)
    if (rendered.contains('.')) {
        while (rendered.endsWith("0")) {
            rendered = rendered.dropLast(1)
        }
        if (rendered.endsWith(".")) {
            rendered = rendered.dropLast(1)
        }
    }
    return rendered
}

internal fun avoidShieldDefaultPercent(): Int = settingsPercent(Settings.avoidShieldThreshold())

internal fun targetShieldDefaultPercent(): Int = settingsPercent(Settings.targetShieldThreshold())

internal fun opportunistKineticDefaultPercent(): Int =
    settingsPercent(Settings.opportunistKineticThreshold())

internal fun opportunistHighExplosiveDefaultPercent(): Int =
    settingsPercent(Settings.opportunistHEThreshold())

internal fun opportunistTriggerDefaultPercent(): Int =
    settingsPercent(Settings.opportunistModifier(), min = 10, max = 500)

internal fun noPdWasteCleanupDamageCapDefault(): Int =
    Settings.noPDWasteCleanupDamageCap().roundToInt().coerceIn(0, 10000)

internal fun priorityMultiplierDefault(): Int =
    Settings.prioXModifier().roundToInt().coerceIn(1, 10000)

internal fun totalFluxCapDefaultPercent(): Int =
    settingsPercent(Settings.softFluxTotalFluxCap(), min = 1, max = 100)
