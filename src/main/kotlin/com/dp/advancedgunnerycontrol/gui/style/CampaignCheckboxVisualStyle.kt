package com.dp.advancedgunnerycontrol.gui.style

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.ButtonAPI
import java.awt.Color
import java.lang.reflect.Method

private const val CHECKED_IDLE_DIM_MULT = 0.55f
private const val TAG_MODE_BORDER_THICKNESS = -1f

internal object CampaignCheckboxVisualStyle {
    private val log = Global.getLogger(CampaignCheckboxVisualStyle::class.java)
    private val buttonOverrideMethodsByClass = mutableMapOf<Class<*>, ButtonOverrideMethods?>()

    /**
     * Decompiled addAreaCheckbox(base, bg, bright) behavior:
     * - base controls the hover/glow color.
     * - bg controls checked fill and border.
     * - bright controls the built-in label text color, which AGC leaves blank here.
     *
     * The Starsector area-checkbox renderer draws a black base, checked fill, border,
     * and then an inset glow while hovered. Negative border thickness is intentional
     * here: it expands the black/glow rectangles to reduce the bright-ring/black-center
     * appearance that otherwise shows on blank-label tag and ship-mode buttons.
     *
     * Weapon tags and ship modes are the same area-checkbox widget class in checked
     * and unchecked states. Starsector does not darken checked controls the same
     * way it darkens normal button idle interiors, so checked state uses AGC's
     * derived rendered color for the backing panel, checkbox fill, border, and
     * glow override.
     */
    fun toggleableCheckboxColors(): CampaignGuiStyle.CheckboxColors {
        return CampaignGuiStyle.CheckboxColors(
            base = CampaignGuiStyle.INACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR,
            bg = CampaignGuiStyle.ACTIVE_WEAPON_TAG_SHIP_MODE_RENDERED_COLOR,
            bright = CampaignGuiStyle.ACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR
        )
    }

    fun artificiallyDarkenedIdleColor(color: Color): Color {
        return Color(
            (color.red * CHECKED_IDLE_DIM_MULT).toInt().coerceIn(0, 255),
            (color.green * CHECKED_IDLE_DIM_MULT).toInt().coerceIn(0, 255),
            (color.blue * CHECKED_IDLE_DIM_MULT).toInt().coerceIn(0, 255),
            color.alpha
        )
    }

    fun applyToggleableCheckboxVisualState(button: ButtonAPI) {
        val glowColor = if (button.isChecked) {
            CampaignGuiStyle.ACTIVE_WEAPON_TAG_SHIP_MODE_RENDERED_COLOR
        } else {
            CampaignGuiStyle.INACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR
        }
        val borderColor = if (button.isChecked) {
            CampaignGuiStyle.ACTIVE_WEAPON_TAG_SHIP_MODE_RENDERED_COLOR
        } else {
            CampaignGuiStyle.INACTIVE_WEAPON_TAG_SHIP_MODE_COLOR
        }
        applyCheckboxVisualOverrides(button, glowColor, borderColor, TAG_MODE_BORDER_THICKNESS)
    }

    fun applyCheckboxVisualOverrides(
        button: ButtonAPI,
        glowColor: Color,
        borderColor: Color,
        borderThickness: Float,
    ) {
        val methods = buttonOverrideMethods(button) ?: return
        invokeColorOverride(button, methods.setGlowOverride, glowColor)
        invokeColorOverride(button, methods.setBorderOverride, borderColor)
        invokeFloatOverride(button, methods.setBorderThickness, borderThickness)
    }

    fun applyUnavailableCheckboxVisualState(button: ButtonAPI) {
        val methods = buttonOverrideMethods(button) ?: return
        invokeColorOverride(button, methods.setGlowOverride, CampaignGuiStyle.DISABLED_TAG_BRIGHT_COLOR)
        invokeColorOverride(button, methods.setBorderOverride, CampaignGuiStyle.DISABLED_TAG_BORDER_COLOR)
        invokeFloatOverride(button, methods.setBorderThickness, 1f)
    }

    private fun buttonOverrideMethods(button: ButtonAPI): ButtonOverrideMethods? {
        val buttonClass = button.javaClass
        if (buttonOverrideMethodsByClass.containsKey(buttonClass)) {
            return buttonOverrideMethodsByClass[buttonClass]
        }
        val methods = try {
            ButtonOverrideMethods(
                setGlowOverride = buttonClass.getMethod("setGlowOverride", Color::class.java),
                setBorderOverride = buttonClass.getMethod("setBorderOverride", Color::class.java),
                setBorderThickness = buttonClass.getMethod("setBorderThickness", java.lang.Float.TYPE),
            )
        } catch (_: Throwable) {
            log.info("AGC area-checkbox color override methods are unavailable for ${buttonClass.name}; using stock checkbox colors for this UI class.")
            null
        }
        buttonOverrideMethodsByClass[buttonClass] = methods
        return methods
    }

    private fun invokeColorOverride(button: ButtonAPI, method: Method, color: Color) {
        try {
            method.invoke(button, color)
        } catch (ex: Throwable) {
            log.warn("AGC failed to apply area-checkbox color override ${method.name}", ex)
        }
    }

    private fun invokeFloatOverride(button: ButtonAPI, method: Method, value: Float) {
        try {
            method.invoke(button, value)
        } catch (ex: Throwable) {
            log.warn("AGC failed to apply area-checkbox float override ${method.name}", ex)
        }
    }

    private data class ButtonOverrideMethods(
        val setGlowOverride: Method,
        val setBorderOverride: Method,
        val setBorderThickness: Method,
    )
}
