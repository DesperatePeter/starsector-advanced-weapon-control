package com.dp.advancedgunnerycontrol.gui.style

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.ACTIVE_FILTER_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.ADD_TAG_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.CANCEL_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.CONFIRM_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.DEFAULT_TEXT_COLOUR
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.DISABLED_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.DISABLED_TAG_TEXT_COLOR
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.FILTER_CATEGORY_ACTIVE_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.FILTER_CATEGORY_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.LOAD_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.SAVE_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.SUGGESTED_FILTER_ACTIVE_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.TAG_CATEGORY_ACTIVE_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.TAG_CATEGORY_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.TAG_TOP_LEVEL_SECTION_BUTTON_COLORS
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle.UNCOLOURED_BUTTON_COLORS
import java.awt.Color
import org.lwjgl.input.Keyboard

internal object CampaignGuiActionButtonStyles {
    fun checkboxColorsForButton(colors: CampaignGuiStyle.ButtonStateColors): CampaignGuiStyle.CheckboxColors {
        return CampaignGuiStyle.CheckboxColors(
            base = colors.hover,
            bg = colors.idle,
            bright = colors.hover
        )
    }

    fun colorsForActionButton(kind: CampaignActionButtonKind): CampaignGuiStyle.ButtonStateColors {
        return when (kind) {
            CampaignActionButtonKind.SAVE -> SAVE_BUTTON_COLORS
            CampaignActionButtonKind.LOAD -> LOAD_BUTTON_COLORS
            CampaignActionButtonKind.CONFIRM -> CONFIRM_BUTTON_COLORS
            CampaignActionButtonKind.CANCEL -> CANCEL_BUTTON_COLORS
            CampaignActionButtonKind.ACTIVE -> ACTIVE_FILTER_BUTTON_COLORS
            CampaignActionButtonKind.FILTER_CATEGORY -> FILTER_CATEGORY_BUTTON_COLORS
            CampaignActionButtonKind.FILTER_CATEGORY_ACTIVE -> FILTER_CATEGORY_ACTIVE_BUTTON_COLORS
            CampaignActionButtonKind.SUGGESTED_FILTER_ACTIVE -> SUGGESTED_FILTER_ACTIVE_BUTTON_COLORS
            CampaignActionButtonKind.TAG_TOP_LEVEL_SECTION -> TAG_TOP_LEVEL_SECTION_BUTTON_COLORS
            CampaignActionButtonKind.TAG_CATEGORY -> TAG_CATEGORY_BUTTON_COLORS
            CampaignActionButtonKind.TAG_CATEGORY_ACTIVE -> TAG_CATEGORY_ACTIVE_BUTTON_COLORS
            CampaignActionButtonKind.ADD_TAG -> ADD_TAG_BUTTON_COLORS
            CampaignActionButtonKind.DISABLED -> DISABLED_BUTTON_COLORS
            CampaignActionButtonKind.UNCOLOURED -> UNCOLOURED_BUTTON_COLORS
        }
    }

    fun shouldFillActionButtonIdle(kind: CampaignActionButtonKind): Boolean {
        return true
    }

    fun actionButtonTemplate(
        kind: CampaignActionButtonKind,
        enabled: Boolean = true,
        disabledKind: CampaignActionButtonKind = CampaignActionButtonKind.DISABLED,
    ): CampaignGuiStyle.ActionButtonTemplate {
        return CampaignGuiStyle.ActionButtonTemplate(
            kind = if (enabled) kind else disabledKind,
            textColor = if (enabled) DEFAULT_TEXT_COLOUR else DISABLED_TAG_TEXT_COLOR,
            enabled = enabled
        )
    }

    fun renderedIdleFillColorForActionButton(kind: CampaignActionButtonKind): Color {
        return CampaignCheckboxVisualStyle.artificiallyDarkenedIdleColor(colorsForActionButton(kind).idle)
    }

    fun colorsForModalComponentButton(kind: CampaignActionButtonKind): CampaignGuiStyle.ButtonStateColors {
        return if (kind == CampaignActionButtonKind.UNCOLOURED) {
            CampaignGuiStyle.MODAL_COMPONENT_BUTTON_COLORS
        } else {
            colorsForActionButton(kind)
        }
    }

    fun renderedIdleFillColorForModalComponentButton(kind: CampaignActionButtonKind): Color {
        return CampaignCheckboxVisualStyle.artificiallyDarkenedIdleColor(colorsForModalComponentButton(kind).idle)
    }

    fun formatActionShortcutName(keyCode: Int): String {
        val rawName = Keyboard.getKeyName(keyCode)
        val uppercase = rawName.uppercase()
        return when {
            uppercase == "DELETE" -> "DEL"
            uppercase.contains("RIGHT") -> "RIGHT"
            uppercase.contains("LEFT") -> "LEFT"
            else -> uppercase
        }
    }

    fun actionLabelText(label: String, shortcuts: List<Int>): String {
        val shortcutText = shortcuts.joinToString(" ") { "[${formatActionShortcutName(it)}]" }
        return if (shortcutText.isBlank()) label else "$label $shortcutText"
    }
}
