package com.dp.advancedgunnerycontrol.gui.style

import com.dp.advancedgunnerycontrol.gui.foundation.WrapGridMetrics
import com.dp.advancedgunnerycontrol.gui.foundation.WrappedLabelLayout
import java.awt.Color
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.util.Misc

private val NEUTRAL_UI_BORDER_COLOR: Color = Color(200, 200, 200)

enum class CampaignBorderMode {
    NONE,
    FULL,
    SIDES,
    SIDES_AND_BOTTOM,
}

enum class CampaignPanelType(val outlineColor: Color) {
    SHIP_WEAPON_GROUPS_PANEL(Color(0, 220, 120)), // debug outline: bright green
    LEFT_COLUMN_PANEL(Color.GRAY), // debug outline: gray
    WEAPON_GROUPS_PANEL(Color(70, 220, 220)), // debug outline: cyan
    WEAPON_GROUP_PANEL(Color.GRAY), // debug outline: gray
    WEAPON_PANEL(Color(255, 220, 120)), // debug outline: pale gold
    WEAPON_ENTRY_PANEL(Color(255, 190, 90)), // debug outline: orange gold
    WEAPON_GROUP_TAG_LIST_PANEL(Color(160, 255, 180)), // debug outline: pale green
    SUGGESTED_WEAPON_PANELS_PANEL(Color(70, 220, 220)), // debug outline: cyan
    SUGGESTED_WEAPON_PANEL(Color.GRAY), // debug outline: gray
    SUGGESTED_TAG_LIST_PANEL(Color(160, 255, 180)), // debug outline: pale green
    SHIP_MODES_PANEL(Color(90, 150, 255)), // debug outline: sky blue
    CONTROL_PANEL(NEUTRAL_UI_BORDER_COLOR), // neutral outline
    PANEL_HEADING(Color.GRAY), // debug outline: gray
    SHIP_PANEL(Color(190, 110, 255)), // debug outline: purple
    OPTIONS_PANEL(Color(255, 110, 110)), // debug outline: salmon
}

enum class CampaignActionButtonKind {
    UNCOLOURED,
    SAVE,
    LOAD,
    CONFIRM,
    CANCEL,
    ACTIVE,
    FILTER_CATEGORY,
    FILTER_CATEGORY_ACTIVE,
    SUGGESTED_FILTER_ACTIVE,
    TAG_TOP_LEVEL_SECTION,
    TAG_CATEGORY,
    TAG_CATEGORY_ACTIVE,
    ADD_TAG,
    DISABLED,
}

val campaignBorderModeByType = mapOf(
    CampaignPanelType.CONTROL_PANEL to CampaignBorderMode.FULL,
    CampaignPanelType.PANEL_HEADING to CampaignBorderMode.FULL,
    CampaignPanelType.LEFT_COLUMN_PANEL to CampaignBorderMode.SIDES_AND_BOTTOM,
    CampaignPanelType.WEAPON_GROUP_PANEL to CampaignBorderMode.SIDES,
    CampaignPanelType.SUGGESTED_WEAPON_PANEL to CampaignBorderMode.SIDES,
).withDefault { CampaignBorderMode.NONE }

object CampaignGuiStyle {
    // Debug-color and button-template models
    data class DebugColorTarget(
        val key: String,
        val label: String,
        val defaultColor: Color,
    )
    data class CheckboxColors(
        val base: Color,
        val bg: Color,
        val bright: Color,
    )
    data class ButtonStateColors(
        val idle: Color,
        val hover: Color,
    )
    data class ActionButtonTemplate(
        val kind: CampaignActionButtonKind,
        val textColor: Color,
        val enabled: Boolean,
    )

    // Core text and panel colors
    val DEFAULT_TEXT_COLOUR: Color = Color(220, 220, 220) // light gray
    val MODIFIER_TEXT_COLOUR: Color = Misc.getHighlightColor()
    val DEFAULT_TEXT_FONT: String? = null
    val TOOLTIP_TEXT_FONT: String? = null
    val BLACK_PANEL_FILL_COLOR: Color = Color(0, 0, 0, 225)
    val TRANSPARENT_PANEL_FILL_COLOR: Color = Color(0, 0, 0, 0)
    val MODAL_BACKDROP_FILL_COLOR: Color = Color(0, 0, 0, 175)
    val MODAL_DIALOG_FILL_COLOR: Color = Color(38, 38, 38, 245)
    val WHITE_PANEL_BORDER_COLOR: Color = NEUTRAL_UI_BORDER_COLOR

    val TOOLTIP_TEXT_COLOR: Color = Color(245, 230, 150) // pale gold
    val ALERT_RED_COLOR: Color get() = CampaignGuiDebugColors.alertRedColor() // alert red
    val DISABLED_TAG_BACKGROUND_COLOR: Color get() = CampaignGuiDebugColors.disabledTagBackgroundColor() // charcoal
    val DISABLED_TAG_DARK_COLOR: Color get() = CampaignGuiDebugColors.disabledTagDarkColor() // near black
    val DISABLED_TAG_BRIGHT_COLOR: Color get() = ALERT_RED_COLOR // alert red
    val DISABLED_TAG_TEXT_COLOR: Color = Color(120, 120, 120) // medium gray
    val DISABLED_TAG_BORDER_COLOR: Color = Color(95, 95, 95) // dark gray
    val SAVE_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.saveButtonIdleColor() // save/apply purple
    val SAVE_BUTTON_HOVER_COLOR: Color get() = SAVE_BUTTON_IDLE_COLOR
    val LOAD_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.loadButtonIdleColor() // load/delete yellow
    val LOAD_BUTTON_HOVER_COLOR: Color get() = LOAD_BUTTON_IDLE_COLOR
    val CONFIRM_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.confirmButtonIdleColor() // confirm green
    val CONFIRM_BUTTON_HOVER_COLOR: Color get() = CONFIRM_BUTTON_IDLE_COLOR
    val EXTERNAL_TOGGLE_ACTIVE_IDLE_COLOR: Color get() = CONFIRM_BUTTON_IDLE_COLOR // muted green
    val EXTERNAL_TOGGLE_ACTIVE_HOVER_COLOR: Color get() = EXTERNAL_TOGGLE_ACTIVE_IDLE_COLOR
    val CANCEL_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.cancelButtonIdleColor() // cancel red
    val CANCEL_BUTTON_HOVER_COLOR: Color get() = CANCEL_BUTTON_IDLE_COLOR
    val UNCOLOURED_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.uncolouredButtonIdleColor() // neutral gray
    val UNCOLOURED_BUTTON_HOVER_COLOR: Color get() = UNCOLOURED_BUTTON_IDLE_COLOR
    val PRESET_SCOPE_BUTTON_IDLE_COLOR: Color get() = CampaignGuiDebugColors.presetScopeButtonIdleColor() // preset orange
    val PRESET_SCOPE_BUTTON_HOVER_COLOR: Color get() = PRESET_SCOPE_BUTTON_IDLE_COLOR

    // Tag, ship-mode, and filter colors
    val INACTIVE_WEAPON_TAG_SHIP_MODE_COLOR: Color get() = UNCOLOURED_BUTTON_IDLE_COLOR
    val INACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR: Color get() = UNCOLOURED_BUTTON_HOVER_COLOR
    val ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR: Color get() = CampaignGuiDebugColors.activeWeaponTagShipModeColor() // active green
    val ACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR: Color get() = ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR
    val ACTIVE_WEAPON_TAG_SHIP_MODE_RENDERED_COLOR: Color get() = CampaignCheckboxVisualStyle.artificiallyDarkenedIdleColor(ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR)
    val ACTIVE_FILTER_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(
        ACTIVE_WEAPON_TAG_SHIP_MODE_COLOR,
        ACTIVE_WEAPON_TAG_SHIP_MODE_HOVER_COLOR
    )
    val ACTIVE_WEAPON_TAG_SHIP_MODE_BUTTON_COLORS: ButtonStateColors get() = ACTIVE_FILTER_BUTTON_COLORS

    val PANEL_HEADING_COLOUR: Color get() = CampaignGuiDebugColors.panelHeadingColor() // dark gray
    val STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR: Color get() = CampaignGuiDebugColors.staleWeaponGroupPanelHeadingColor() // stale gray
    val COLLAPSIBLE_HEADING_COLOUR: Color get() = CampaignGuiDebugColors.collapsibleHeadingColor() // collapsible heading
    val UNSAVED_COLLAPSIBLE_HEADING_COLOUR: Color get() = CampaignGuiDebugColors.unsavedCollapsibleHeadingColor() // unsaved heading
    val FILTER_CATEGORY_INACTIVE_COLOR: Color get() = CampaignGuiDebugColors.suggestedFilterHeadingColor() // suggested filter heading
    val SUGGESTED_FILTER_ACTIVE_COLOR: Color get() = CampaignGuiDebugColors.suggestedFilterActiveColor() // active suggested filter
    val TOP_LEVEL_TAG_SHIP_MODE_HEADING_COLOR: Color get() = CampaignGuiDebugColors.topLevelTagShipModeHeadingColor() // top-level tag/mode heading
    val SPECIFIC_TAG_SHIP_MODE_HEADING_COLOR: Color get() = CampaignGuiDebugColors.specificTagShipModeHeadingColor() // tag category heading
    const val CHILD_ROW_INDENT: Float = 28.5f
    const val FILTER_CHILD_ROW_INDENT: Float = CHILD_ROW_INDENT
    fun sameColorButtonState(color: Color): ButtonStateColors = ButtonStateColors(color, color)

    val FILTER_CATEGORY_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(FILTER_CATEGORY_INACTIVE_COLOR)
    val FILTER_CATEGORY_ACTIVE_BUTTON_COLORS: ButtonStateColors get() = FILTER_CATEGORY_BUTTON_COLORS
    val SUGGESTED_FILTER_ACTIVE_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(
        SUGGESTED_FILTER_ACTIVE_COLOR,
        SUGGESTED_FILTER_ACTIVE_COLOR
    )
    val TAG_TOP_LEVEL_SECTION_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(TOP_LEVEL_TAG_SHIP_MODE_HEADING_COLOR)
    val TAG_CATEGORY_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(SPECIFIC_TAG_SHIP_MODE_HEADING_COLOR)
    val TAG_CATEGORY_ACTIVE_BUTTON_COLORS: ButtonStateColors get() = TAG_CATEGORY_BUTTON_COLORS
    val ADD_TAG_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(STALE_WEAPON_GROUP_PANEL_HEADING_COLOUR)
    val MODAL_COMPONENT_ROW_FILL_COLOR: Color get() = BLACK_PANEL_FILL_COLOR
    val MODAL_COMPONENT_BORDER_COLOR: Color get() = NEUTRAL_UI_BORDER_COLOR
    val MODAL_SCROLL_LIST_FILL_COLOR: Color get() = BLACK_PANEL_FILL_COLOR
    val MODAL_SCROLL_LIST_BORDER_COLOR: Color get() = NEUTRAL_UI_BORDER_COLOR
    val MODAL_REVIEW_LIST_FILL_COLOR: Color get() = MODAL_DIALOG_FILL_COLOR
    val MODAL_COMPONENT_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(DEFAULT_TEXT_COLOUR)

    // Derived action-button color families
    val SAVE_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(SAVE_BUTTON_IDLE_COLOR, SAVE_BUTTON_HOVER_COLOR)
    val LOAD_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(LOAD_BUTTON_IDLE_COLOR, LOAD_BUTTON_HOVER_COLOR)
    val CONFIRM_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(CONFIRM_BUTTON_IDLE_COLOR, CONFIRM_BUTTON_HOVER_COLOR)
    val EXTERNAL_TOGGLE_ACTIVE_COLORS: ButtonStateColors get() = ButtonStateColors(EXTERNAL_TOGGLE_ACTIVE_IDLE_COLOR, EXTERNAL_TOGGLE_ACTIVE_HOVER_COLOR)
    val CANCEL_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(CANCEL_BUTTON_IDLE_COLOR, CANCEL_BUTTON_HOVER_COLOR)
    val UNCOLOURED_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(UNCOLOURED_BUTTON_IDLE_COLOR, UNCOLOURED_BUTTON_HOVER_COLOR)
    val PRESET_SCOPE_BUTTON_COLORS: ButtonStateColors get() = ButtonStateColors(PRESET_SCOPE_BUTTON_IDLE_COLOR, PRESET_SCOPE_BUTTON_HOVER_COLOR)
    val DISABLED_BUTTON_COLORS: ButtonStateColors get() = sameColorButtonState(DISABLED_TAG_BACKGROUND_COLOR)
    val PRESET_TOGGLE_INACTIVE_COLORS: ButtonStateColors get() = UNCOLOURED_BUTTON_COLORS

    // Shared layout constants
    const val MAIN_PADDING = 0f
    const val PANEL_PADDING = 4f
    const val TAG_ITEM_HEIGHT = 20f
    const val TAG_ITEM_HGAP = 0f
    const val TAG_ITEM_VGAP = 2f // Dense weapon-tag grids use tighter spacing to keep seven columns readable.
    const val TAG_ITEM_MIN_WIDTH = 96f
    const val SHIP_MODE_ITEM_HEIGHT = 20f
    const val SHIP_MODE_ITEM_HGAP = 0f
    const val SHIP_MODE_ITEM_VGAP = 4f // Single-column ship-mode rows get modal-style breathing room.
    const val SHIP_MODE_ITEM_MIN_WIDTH = 78f
    const val SHIP_MODE_COLUMN_COUNT = 1
    const val ITEM_TEXT_HORIZONTAL_PADDING = 4f
    const val ITEM_TEXT_TOP_PADDING = 2f
    const val ITEM_HIGHLIGHT_X_OFFSET = -3f
    const val CENTERED_LABEL_CHAR_WIDTH_ESTIMATE = 6.8f
    const val SHIP_MODE_LABEL_CHAR_WIDTH_ESTIMATE = 5.8f
    const val HEADING_CHAR_WIDTH_ESTIMATE = 7.2f
    const val WEAPON_ENTRY_TOOLTIP_WIDTH = 390f
    const val STANDARD_TOOLTIP_WIDTH = WEAPON_ENTRY_TOOLTIP_WIDTH
    const val SUGGESTED_FILTER_INFO_HEIGHT = 42f
    const val WEAPON_TAG_SCROLL_STEP = 2
    const val SUGGESTED_FILTER_SCROLL_STEP = 3
    const val SCROLL_INDICATOR_ABOVE = "^     ^     ^     ^     ^"
    const val SCROLL_INDICATOR_BELOW = "v     v     v     v     v"
    const val GUI_PERF_LOG_THRESHOLD_MS = 25L
    const val ACTION_ROW_GAP = 2f // Dense option/action panels match tag-list tightness.
    const val ACTION_ROW_PADDING = 4f
    const val ACTION_LABEL_APPROX_CHAR_WIDTH = 6.4f
    const val ACTION_LABEL_LINE_HEIGHT = 15f
    const val MODAL_BODY_LINE_HEIGHT = 18f
    const val CONTAINER_HEADING_HEIGHT = 20f
    const val ACTION_LABEL_MAX_LINES = 2
    const val SUGGESTED_ACTION_LABEL_MAX_LINES = 2
    const val MODAL_PADDING = 14f
    const val MODAL_ROW_HEIGHT = 22f
    const val MODAL_ROW_GAP = 4f
    const val MODAL_SECTION_GAP = MODAL_ROW_HEIGHT / 2f
    const val MODAL_TIGHT_GAP = MODAL_ROW_GAP
    const val MODAL_RELATED_CONTROL_GAP = MODAL_ROW_GAP
    const val MODAL_TEXT_COMPONENT_GAP = 2f * MODAL_ROW_GAP
    const val MODAL_BODY_CONTROL_GAP = MODAL_TEXT_COMPONENT_GAP
    const val MODAL_TITLE_BODY_GAP = MODAL_TEXT_COMPONENT_GAP
    const val MODAL_SECTION_BREAK_GAP = MODAL_TITLE_BODY_GAP
    const val MODAL_BODY_ACTION_GAP = MODAL_BODY_CONTROL_GAP
    const val MODAL_COMPONENT_HORIZONTAL_INSET = ITEM_TEXT_HORIZONTAL_PADDING
    const val MODAL_FOOTER_HORIZONTAL_INSET = MODAL_COMPONENT_HORIZONTAL_INSET
    const val MODAL_MAX_HEIGHT_FRACTION = 0.8f
    const val MODAL_BUTTON_WIDTH = 120f
    const val MODAL_HEADING_HEIGHT = 30f
    // Modal dimensions
    // Keep edit-tag style labelled components close to a 50/50 label/value split.
    const val EDIT_TAG_LABEL_WIDTH = 248f
    const val EDIT_TAG_VALUE_GAP = 8f
    const val EDIT_TAG_VALUE_WIDTH = 248f
    const val EDIT_TAG_MODAL_WIDTH = 2f * MODAL_PADDING + EDIT_TAG_LABEL_WIDTH + EDIT_TAG_VALUE_GAP + EDIT_TAG_VALUE_WIDTH
    const val ROOT_CONTENT_MIN_WIDTH = 300f
    const val ROOT_CONTENT_MIN_HEIGHT = 300f
    const val ROOT_CONTENT_MIN_HEIGHT_FRACTION = 0.8f
    fun maxModalHeight(screenHeight: Float, minimum: Float = MODAL_ROW_HEIGHT): Float {
        return CampaignGuiLayoutStyle.maxModalHeight(screenHeight, minimum)
    }
    fun modalHeightForBody(
        bodyHeight: Float,
        headingHeight: Float = MODAL_HEADING_HEIGHT,
        footerHeight: Float = MODAL_ROW_HEIGHT,
        titleBodyGap: Float = MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = MODAL_BODY_ACTION_GAP,
    ): Float {
        return CampaignGuiLayoutStyle.modalHeightForBody(
            bodyHeight = bodyHeight,
            headingHeight = headingHeight,
            footerHeight = footerHeight,
            titleBodyGap = titleBodyGap,
            bodyActionGap = bodyActionGap,
        )
    }
    fun modalFixedHeightExcludingBody(
        headingHeight: Float = MODAL_HEADING_HEIGHT,
        footerHeight: Float = MODAL_ROW_HEIGHT,
        titleBodyGap: Float = MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = MODAL_BODY_ACTION_GAP,
    ): Float {
        return CampaignGuiLayoutStyle.modalFixedHeightExcludingBody(
            headingHeight = headingHeight,
            footerHeight = footerHeight,
            titleBodyGap = titleBodyGap,
            bodyActionGap = bodyActionGap,
        )
    }
    fun modalBodyHeightForDialog(
        dialogHeight: Float,
        headingHeight: Float = MODAL_HEADING_HEIGHT,
        footerHeight: Float = MODAL_ROW_HEIGHT,
        minimumBodyHeight: Float = MODAL_ROW_HEIGHT,
        titleBodyGap: Float = MODAL_TITLE_BODY_GAP,
        bodyActionGap: Float = MODAL_BODY_ACTION_GAP,
    ): Float {
        return CampaignGuiLayoutStyle.modalBodyHeightForDialog(
            dialogHeight = dialogHeight,
            headingHeight = headingHeight,
            footerHeight = footerHeight,
            minimumBodyHeight = minimumBodyHeight,
            titleBodyGap = titleBodyGap,
            bodyActionGap = bodyActionGap,
        )
    }
    const val ERROR_FALLBACK_MIN_WIDTH = 500f
    const val ERROR_FALLBACK_HEIGHT = 220f
    const val ERROR_FALLBACK_PADDING = 8f

    // Text highlighting
    val ACTION_SHORTCUT_HIGHLIGHTS = listOf(
        "[TAB]",
        "[DELETE]",
        "[DEL]",
        "[ESCAPE]",
        "[LEFT]",
        "[LEFT ARROW]",
        "[D]",
        "[A]",
        "[RIGHT]",
        "[RIGHT ARROW]",
        "RIGHT",
        "LEFT",
        "[F]",
    )

    fun debugColorTargets(): List<DebugColorTarget> = CampaignGuiDebugColors.debugColorTargets()

    fun currentDebugColor(target: DebugColorTarget): Color {
        return CampaignGuiDebugColors.currentDebugColor(target)
    }

    fun setDebugColorOverride(target: DebugColorTarget, color: Color, persistent: Boolean = false) {
        CampaignGuiDebugColors.setDebugColorOverride(target, color, persistent)
    }

    fun checkboxColorsForButton(colors: ButtonStateColors): CheckboxColors {
        return CampaignGuiActionButtonStyles.checkboxColorsForButton(colors)
    }

    fun colorsForActionButton(kind: CampaignActionButtonKind): ButtonStateColors {
        return CampaignGuiActionButtonStyles.colorsForActionButton(kind)
    }

    fun shouldFillActionButtonIdle(kind: CampaignActionButtonKind): Boolean {
        return CampaignGuiActionButtonStyles.shouldFillActionButtonIdle(kind)
    }

    fun actionButtonTemplate(
        kind: CampaignActionButtonKind,
        enabled: Boolean = true,
        disabledKind: CampaignActionButtonKind = CampaignActionButtonKind.DISABLED,
    ): ActionButtonTemplate {
        return CampaignGuiActionButtonStyles.actionButtonTemplate(kind, enabled, disabledKind)
    }

    fun renderedIdleFillColorForActionButton(kind: CampaignActionButtonKind): Color {
        return CampaignGuiActionButtonStyles.renderedIdleFillColorForActionButton(kind)
    }

    fun colorsForModalComponentButton(kind: CampaignActionButtonKind): ButtonStateColors {
        return CampaignGuiActionButtonStyles.colorsForModalComponentButton(kind)
    }

    fun renderedIdleFillColorForModalComponentButton(kind: CampaignActionButtonKind): Color {
        return CampaignGuiActionButtonStyles.renderedIdleFillColorForModalComponentButton(kind)
    }

    fun formatActionShortcutName(keyCode: Int): String {
        return CampaignGuiActionButtonStyles.formatActionShortcutName(keyCode)
    }

    fun actionLabelText(label: String, shortcuts: List<Int>): String {
        return CampaignGuiActionButtonStyles.actionLabelText(label, shortcuts)
    }

    fun actionLabelLayout(labelText: String, width: Float, maxLines: Int): WrappedLabelLayout {
        return CampaignGuiLayoutStyle.actionLabelLayout(labelText, width, maxLines)
    }

    fun campaignTagGridMetrics(tags: List<String>, panelWidth: Float, panelHeight: Float): WrapGridMetrics {
        return CampaignGuiLayoutStyle.campaignTagGridMetrics(tags, panelWidth, panelHeight)
    }

    fun toggleableCheckboxColors(): CheckboxColors {
        return CampaignCheckboxVisualStyle.toggleableCheckboxColors()
    }

    fun applyToggleableCheckboxVisualState(button: ButtonAPI) {
        CampaignCheckboxVisualStyle.applyToggleableCheckboxVisualState(button)
    }

    fun applyCheckboxVisualOverrides(
        button: ButtonAPI,
        glowColor: Color,
        borderColor: Color,
        borderThickness: Float,
    ) {
        CampaignCheckboxVisualStyle.applyCheckboxVisualOverrides(button, glowColor, borderColor, borderThickness)
    }

    fun applyUnavailableCheckboxVisualState(button: ButtonAPI) {
        CampaignCheckboxVisualStyle.applyUnavailableCheckboxVisualState(button)
    }
}
