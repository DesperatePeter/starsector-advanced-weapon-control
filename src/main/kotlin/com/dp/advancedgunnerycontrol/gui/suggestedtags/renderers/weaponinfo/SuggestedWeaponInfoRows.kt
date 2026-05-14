package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import com.fs.starfarer.api.loading.BeamWeaponSpecAPI
import com.fs.starfarer.api.loading.MissileSpecAPI
import com.fs.starfarer.api.loading.ProjectileSpecAPI
import com.fs.starfarer.api.loading.ProjectileWeaponSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

internal object SuggestedWeaponInfoRows {
    fun basicInfoRows(spec: WeaponSpecAPI): List<String> {
        val derived = spec.derivedStats
        val damage = max(derived.burstDamage, derived.damagePerShot)
        val rows = mutableListOf<String>()
        rows += "Name: ${spec.weaponName?.takeIf { it.isNotBlank() } ?: spec.weaponId}"
        addTextRow(rows, "Role", spec.primaryRoleStr)
        rows += "Size: ${spec.size}"
        rows += "Type: ${spec.type}"
        addNumberRow(rows, "OP", runCatching { spec.getOrdnancePointCost(null) }.getOrDefault(0f))
        addNumberRow(rows, "Range", spec.maxRange)
        (spec as? ProjectileWeaponSpecAPI)?.let { addNumberRow(rows, "Refire", it.refireDelay, "s", allowDecimals = true) }
        addNumberRow(rows, "Damage", damage)
        addNumberRow(rows, "Sust DPS", derived.sustainedDps, "(${shortDamageType(spec.damageType?.toString())})")
        addNumberRow(rows, "Sust Flux", derived.sustainedFluxPerSecond)
        addNumberRow(rows, "Flux/Dmg", derived.fluxPerDam, allowDecimals = true)
        addNumberRow(rows, "EMP", max(derived.empPerSecond, derived.empPerShot))
        addAmmoOrChargeRows(rows, spec)
        addTextRow(rows, "Acc", spec.accuracyStr?.takeIf { it.isNotBlank() } ?: accuracyFallback(spec))
        return rows
    }

    fun advancedInfoRows(spec: WeaponSpecAPI): List<String> {
        val derived = spec.derivedStats
        val rows = mutableListOf<String>()
        addNumberRow(rows, "EMP/s", derived.empPerSecond)
        if (derived.empPerSecond > 0f || derived.empPerShot > 0f) {
            addNumberRow(rows, "Flux/EMP", derived.fluxPerSecond / max(derived.empPerSecond, derived.empPerShot), allowDecimals = true)
        }
        (spec as? BeamWeaponSpecAPI)?.let { beam ->
            addNumberRow(rows, "Beam DPS", beam.damagePerSecond)
            addNumberRow(rows, "Beam Spd", beam.beamSpeed)
            addNumberRow(rows, "Charge Up", beam.chargeupTime, allowDecimals = true)
            addNumberRow(rows, "Charge Down", beam.chargedownTime, allowDecimals = true)
        }
        (spec as? ProjectileWeaponSpecAPI)?.let { projectile ->
            addNumberRow(rows, "Burst", projectile.burstDelay, allowDecimals = true)
        }
        addNumberRow(rows, "Turn/s", spec.turnRate, "deg")
        addNumberRow(rows, "Min Spr", spec.minSpread, allowDecimals = true)
        addNumberRow(rows, "Max Spr", spec.maxSpread, allowDecimals = true)
        addNumberRow(rows, "Spr/shot", spec.spreadBuildup, allowDecimals = true)
        addNumberRow(rows, "Spr decay", spec.spreadDecayRate, allowDecimals = true)
        (spec.projectileSpec as? ProjectileSpecAPI)?.let { projectile ->
            addNumberRow(rows, "Proj Spd", projectile.getMoveSpeed(null, null))
            addNumberRow(rows, "Proj HP", projectile.maxHealth)
        }
        (spec.projectileSpec as? MissileSpecAPI)?.let { missile ->
            addNumberRow(rows, "Launch", missile.launchSpeed)
            addNumberRow(rows, "Flight", missile.maxFlightTime, "s", allowDecimals = true)
            rows += "Guided: ${missile.behaviorSpec != null}"
        }
        return rows
    }

    private fun shortDamageType(value: String?): String {
        return when (value) {
            "HIGH_EXPLOSIVE" -> "HE"
            "FRAGMENTATION" -> "Frag"
            null -> ""
            else -> value
        }
    }

    private fun addAmmoOrChargeRows(rows: MutableList<String>, spec: WeaponSpecAPI) {
        if (!spec.usesAmmo() || spec.maxAmmo <= 0) return
        val usesRegeneratingCharges = spec.ammoPerSecond > 0f
        if (usesRegeneratingCharges) {
            rows += "Max Charges: ${spec.maxAmmo}"
            addNumberRow(rows, "Sec / Recharge", 1f / spec.ammoPerSecond, allowDecimals = true)
            addNumberRow(rows, "Charge Gain", spec.reloadSize)
        } else {
            rows += "Max Ammo: ${spec.maxAmmo}"
            if (spec.ammoPerSecond > 0f) addNumberRow(rows, "Sec / Reload", 1f / spec.ammoPerSecond, allowDecimals = true)
            addNumberRow(rows, "Ammo Gain", spec.reloadSize)
        }
    }

    private fun addTextRow(rows: MutableList<String>, label: String, value: String?) {
        val clean = value?.trim().orEmpty()
        if (clean.isNotBlank()) rows += "$label: $clean"
    }

    private fun addNumberRow(
        rows: MutableList<String>,
        label: String,
        value: Float,
        suffix: String = "",
        allowDecimals: Boolean = false,
    ) {
        if (!value.isFinite() || value <= 0f) return
        val suffixText = if (suffix.isBlank()) "" else " $suffix"
        rows += "$label: ${formatStat(value, allowDecimals)}$suffixText"
    }

    private fun accuracyFallback(spec: WeaponSpecAPI): String {
        return if (spec.maxSpread <= 0f && spec.spreadBuildup <= 0f) "Perfect" else formatStat(spec.maxSpread, allowDecimals = true)
    }

    private fun formatStat(value: Float, allowDecimals: Boolean = false): String {
        if (!value.isFinite() || value < 0f) return "0"
        return if (!allowDecimals || abs(value - value.roundToInt()) < 0.005f) {
            value.roundToInt().toString()
        } else {
            String.format(Locale.US, "%.2f", value)
        }
    }
}
