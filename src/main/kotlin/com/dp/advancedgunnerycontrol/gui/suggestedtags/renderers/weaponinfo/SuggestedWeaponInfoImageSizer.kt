package com.dp.advancedgunnerycontrol.gui.suggestedtags.renderers.weaponinfo

import com.fs.starfarer.api.Global
import kotlin.math.min

internal object SuggestedWeaponInfoImageSizer {
    fun fitSprite(spriteName: String?, maxWidth: Float, maxHeight: Float): Pair<Float, Float> {
        if (spriteName.isNullOrBlank()) return 0f to 0f
        return try {
            val sprite = Global.getSettings().getSprite(spriteName)
            if (sprite.width <= 0f || sprite.height <= 0f) {
                min(maxWidth, maxHeight) to min(maxWidth, maxHeight)
            } else {
                val scale = min(maxWidth / sprite.width, maxHeight / sprite.height)
                sprite.width * scale to sprite.height * scale
            }
        } catch (_: Throwable) {
            min(maxWidth, maxHeight) to min(maxWidth, maxHeight)
        }
    }
}
