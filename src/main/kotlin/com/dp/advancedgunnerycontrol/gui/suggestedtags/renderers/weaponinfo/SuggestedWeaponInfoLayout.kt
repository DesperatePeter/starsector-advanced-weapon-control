package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle

internal object SuggestedWeaponInfoLayout {
    const val MIN_RENDERABLE_TAG_LIST_HEIGHT = 48f
    const val WEAPON_INFO_TO_TAG_GAP = 0f

    const val SUGGESTED_TAG_LIST_MIN_HEIGHT = 120f
    const val WEAPON_INFO_MIN_HEIGHT = 90f
    const val WEAPON_INFO_TEXT_COMPONENT_GAP = CampaignGuiStyle.MODAL_TEXT_COMPONENT_GAP
    const val WEAPON_IMAGE_TOP_GAP = 5f
    const val WEAPON_IMAGE_BOTTOM_GAP = 6f
    const val WEAPON_IMAGE_MAX = 52f
    const val WEAPON_INFO_ROW_HEIGHT = 15f
    const val WEAPON_INFO_APPROX_CHAR_WIDTH = 6.2f
    const val BASIC_INFO_MAX_VISIBLE_ROWS = 16
    const val BASIC_INFO_SECTION_HEIGHT =
        CampaignGuiStyle.TAG_ITEM_HEIGHT +
            2f * WEAPON_INFO_TEXT_COMPONENT_GAP +
            BASIC_INFO_MAX_VISIBLE_ROWS * WEAPON_INFO_ROW_HEIGHT
}
