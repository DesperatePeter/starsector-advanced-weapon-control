package com.dp.advancedgunnerycontrol.gui.style

import com.dp.advancedgunnerycontrol.config.Values
import com.fs.starfarer.api.Global
import java.awt.Color
import org.json.JSONObject
import org.lazywizard.lazylib.JSONUtils

private typealias DebugColorTarget = CampaignGuiStyle.DebugColorTarget

private const val ALERT_RED_COLOR_KEY = "ALERT_RED_COLOR"
private const val DISABLED_TAG_BACKGROUND_COLOR_KEY = "DISABLED_TAG_BACKGROUND_COLOR"
private const val DISABLED_TAG_DARK_COLOR_KEY = "DISABLED_TAG_DARK_COLOR"
private const val SAVE_BUTTON_IDLE_COLOR_KEY = "SAVE_BUTTON_IDLE_COLOR"
private const val LOAD_BUTTON_IDLE_COLOR_KEY = "LOAD_BUTTON_IDLE_COLOR"
private const val CONFIRM_BUTTON_IDLE_COLOR_KEY = "CONFIRM_BUTTON_IDLE_COLOR"
private const val CANCEL_BUTTON_IDLE_COLOR_KEY = "CANCEL_BUTTON_IDLE_COLOR"
private const val UNCOLOURED_BUTTON_IDLE_COLOR_KEY = "UNCOLOURED_BUTTON_IDLE_COLOR"
private const val PRESET_SCOPE_BUTTON_IDLE_COLOR_KEY = "PRESET_SCOPE_BUTTON_IDLE_COLOR"
private const val ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR_KEY = "ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR"
private const val PANEL_HEADING_COLOUR_KEY = "PANEL_HEADING_COLOUR"
private const val STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR_KEY = "STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR"
private const val COLLAPSIBLE_HEADING_COLOUR_KEY = "COLLAPSIBLE_HEADING_COLOUR"
private const val UNSAVED_COLLAPSIBLE_HEADING_COLOUR_KEY = "UNSAVED_COLLAPSIBLE_HEADING_COLOUR"
private const val TOP_LEVEL_TAG_SHIP_MODE_HEADING_COLOR_KEY = "SELECTED_WEAPON_SHIP_IDLE_COLOR"
private const val SPECIFIC_TAG_SHIP_MODE_HEADING_COLOR_KEY = "TAG_COLLAPSIBLE_HEADING_COLOUR"
private const val SUGGESTED_FILTER_HEADING_COLOR_KEY = "SUGGESTED_FILTER_HEADING_COLOR"
private const val SUGGESTED_FILTER_ACTIVE_COLOR_KEY = "SUGGESTED_FILTER_ACTIVE_COLOR"

internal object CampaignGuiDebugColors {
    private val log = Global.getLogger(CampaignGuiDebugColors::class.java)
    private val runtimeColorOverrides = mutableMapOf<String, Color>()
    private var persistentDebugColorsLoaded = false
    private val debugColorTargetList = listOf(
        DebugColorTarget(SAVE_BUTTON_IDLE_COLOR_KEY, "Save / Apply buttons", Color(200, 170, 225, 225)),
        DebugColorTarget(LOAD_BUTTON_IDLE_COLOR_KEY, "Load / Delete buttons", Color(205, 190, 105, 225)),
        DebugColorTarget(CONFIRM_BUTTON_IDLE_COLOR_KEY, "Confirm buttons", Color(160, 225, 135, 225)),
        DebugColorTarget(CANCEL_BUTTON_IDLE_COLOR_KEY, "Cancel buttons", Color(225, 120, 120, 225)),
        DebugColorTarget(UNCOLOURED_BUTTON_IDLE_COLOR_KEY, "Neutral buttons", Color(120, 120, 120, 225)),
        DebugColorTarget(PRESET_SCOPE_BUTTON_IDLE_COLOR_KEY, "Preset scope buttons", Color(225, 155, 80, 225)),
        DebugColorTarget(ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR_KEY, "Active weapon tags / ship modes", Color(160, 225, 135, 225)),
        DebugColorTarget(PANEL_HEADING_COLOUR_KEY, "Panel headings", Color(40, 40, 40, 225)),
        DebugColorTarget(STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR_KEY, "Stale group headings", Color(130, 130, 130, 225)),
        DebugColorTarget(COLLAPSIBLE_HEADING_COLOUR_KEY, "Collapsible headings", Color(200, 200, 200, 225)),
        DebugColorTarget(UNSAVED_COLLAPSIBLE_HEADING_COLOUR_KEY, "Unsaved / active headings", Color(200, 200, 200, 225)),
        DebugColorTarget(TOP_LEVEL_TAG_SHIP_MODE_HEADING_COLOR_KEY, "Top-level tag / ship mode headings", Color(200, 200, 200, 225)),
        DebugColorTarget(SPECIFIC_TAG_SHIP_MODE_HEADING_COLOR_KEY, "Specific tag / ship mode headings", Color(80, 150, 220, 225)),
        DebugColorTarget(SUGGESTED_FILTER_HEADING_COLOR_KEY, "Suggested filter headings", Color(225, 155, 80, 225)),
        DebugColorTarget(SUGGESTED_FILTER_ACTIVE_COLOR_KEY, "Active suggested filters", Color(160, 225, 135, 225)),
        DebugColorTarget(ALERT_RED_COLOR_KEY, "Alert red", Color(245, 95, 85, 240)),
        DebugColorTarget(DISABLED_TAG_BACKGROUND_COLOR_KEY, "Disabled tag background", Color(28, 28, 28, 235)),
        DebugColorTarget(DISABLED_TAG_DARK_COLOR_KEY, "Disabled tag checked fill", Color(18, 18, 18, 235)),
    )

    fun alertRedColor(): Color = debugColor(ALERT_RED_COLOR_KEY, Color(245, 95, 85, 240))

    fun disabledTagBackgroundColor(): Color =
        debugColor(DISABLED_TAG_BACKGROUND_COLOR_KEY, Color(28, 28, 28, 235))

    fun disabledTagDarkColor(): Color =
        debugColor(DISABLED_TAG_DARK_COLOR_KEY, Color(18, 18, 18, 235))

    fun saveButtonIdleColor(): Color =
        debugColor(SAVE_BUTTON_IDLE_COLOR_KEY, Color(200, 170, 225, 225))

    fun loadButtonIdleColor(): Color =
        debugColor(LOAD_BUTTON_IDLE_COLOR_KEY, Color(205, 190, 105, 225))

    fun confirmButtonIdleColor(): Color =
        debugColor(CONFIRM_BUTTON_IDLE_COLOR_KEY, Color(160, 225, 135, 225))

    fun cancelButtonIdleColor(): Color =
        debugColor(CANCEL_BUTTON_IDLE_COLOR_KEY, Color(225, 120, 120, 225))

    fun uncolouredButtonIdleColor(): Color =
        debugColor(UNCOLOURED_BUTTON_IDLE_COLOR_KEY, Color(120, 120, 120, 225))

    fun presetScopeButtonIdleColor(): Color =
        debugColor(PRESET_SCOPE_BUTTON_IDLE_COLOR_KEY, Color(225, 155, 80, 225))

    fun activeWeaponTagShipModeColor(): Color =
        debugColor(ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR_KEY, Color(160, 225, 135, 225))

    fun panelHeadingColor(): Color =
        debugColor(PANEL_HEADING_COLOUR_KEY, Color(40, 40, 40, 225))

    fun staleWeaponGroupPanelHeadingColor(): Color =
        debugColor(STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR_KEY, Color(130, 130, 130, 225))

    fun collapsibleHeadingColor(): Color =
        debugColor(COLLAPSIBLE_HEADING_COLOUR_KEY, Color(200, 200, 200, 225))

    fun unsavedCollapsibleHeadingColor(): Color =
        debugColor(UNSAVED_COLLAPSIBLE_HEADING_COLOUR_KEY, Color(200, 200, 200, 225))

    fun suggestedFilterHeadingColor(): Color =
        debugColor(SUGGESTED_FILTER_HEADING_COLOR_KEY, Color(225, 155, 80, 225))

    fun suggestedFilterActiveColor(): Color =
        debugColor(SUGGESTED_FILTER_ACTIVE_COLOR_KEY, Color(160, 225, 135, 225))

    fun topLevelTagShipModeHeadingColor(): Color =
        debugColor(TOP_LEVEL_TAG_SHIP_MODE_HEADING_COLOR_KEY, Color(200, 200, 200, 225))

    fun specificTagShipModeHeadingColor(): Color =
        debugColor(SPECIFIC_TAG_SHIP_MODE_HEADING_COLOR_KEY, Color(80, 150, 220, 225))

    fun debugColorTargets(): List<DebugColorTarget> = debugColorTargetList

    fun currentDebugColor(target: DebugColorTarget): Color {
        ensurePersistentDebugColorsLoaded()
        return runtimeColorOverrides[target.key] ?: target.defaultColor
    }

    fun setDebugColorOverride(target: DebugColorTarget, color: Color, persistent: Boolean = false) {
        val normalized = Color(
            color.red.coerceIn(0, 255),
            color.green.coerceIn(0, 255),
            color.blue.coerceIn(0, 255),
            target.defaultColor.alpha
        )
        runtimeColorOverrides[target.key] = normalized
        if (persistent) {
            savePersistentDebugColor(target, normalized)
        }
    }

    private fun debugColor(key: String, defaultColor: Color): Color {
        ensurePersistentDebugColorsLoaded()
        return runtimeColorOverrides[key] ?: defaultColor
    }

    private fun ensurePersistentDebugColorsLoaded() {
        if (persistentDebugColorsLoaded) return
        persistentDebugColorsLoaded = true
        try {
            val root = JSONUtils.loadCommonJSON(Values.DEBUG_GUI_COLORS_JSON_FILE_NAME)
            debugColorTargetList.forEach { target ->
                val entry = root.optJSONObject(target.key) ?: return@forEach
                runtimeColorOverrides[target.key] = Color(
                    entry.optInt("r", target.defaultColor.red).coerceIn(0, 255),
                    entry.optInt("g", target.defaultColor.green).coerceIn(0, 255),
                    entry.optInt("b", target.defaultColor.blue).coerceIn(0, 255),
                    target.defaultColor.alpha
                )
            }
        } catch (ex: Throwable) {
            log.warn("Unable to load ${Values.DEBUG_GUI_COLORS_JSON_FILE_NAME}", ex)
        }
    }

    private fun savePersistentDebugColor(target: DebugColorTarget, color: Color) {
        try {
            val root = JSONUtils.loadCommonJSON(Values.DEBUG_GUI_COLORS_JSON_FILE_NAME)
            root.put(
                target.key,
                JSONObject()
                    .put("r", color.red)
                    .put("g", color.green)
                    .put("b", color.blue)
            )
            root.save()
        } catch (ex: Throwable) {
            log.warn("Unable to save ${Values.DEBUG_GUI_COLORS_JSON_FILE_NAME}", ex)
        }
    }
}
