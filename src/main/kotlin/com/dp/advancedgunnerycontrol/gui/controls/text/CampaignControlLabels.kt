package com.dp.advancedgunnerycontrol.gui.controls.text

import com.dp.advancedgunnerycontrol.gui.controls.rows.StyledCampaignButtonShell
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.entrypoints.AGCGUI
import com.dp.advancedgunnerycontrol.gui.foundation.addAgcText
import com.dp.advancedgunnerycontrol.gui.foundation.computeTextFitLayout
import com.dp.advancedgunnerycontrol.gui.foundation.CampaignPanelPlugin
import com.dp.advancedgunnerycontrol.gui.style.CampaignGuiStyle
import com.dp.advancedgunnerycontrol.gui.style.CampaignPanelType
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

object CampaignControlLabels {
fun addCampaignActionLabel(
    panel: TooltipMakerAPI,
    labelText: String,
    highlightTokens: List<String>,
    textColor: Color = CampaignGuiStyle.DEFAULT_TEXT_COLOUR,
) {
    val label = panel.addAgcText(labelText, 0f, textColor)
    val highlights = highlightTokens.filter { labelText.contains(it) }
    if (highlights.isNotEmpty()) {
        label.setHighlight(*highlights.toTypedArray())
        label.setHighlightColors(*Array(highlights.size) { CampaignGuiStyle.MODIFIER_TEXT_COLOUR })
    }
}

fun hiddenChangeHighlightTokens(labelText: String, baseTokens: List<String> = emptyList()): List<String> {
    if (!labelText.startsWith("*")) return baseTokens
    if ("*" in baseTokens) return baseTokens
    return listOf("*") + baseTokens
}

fun renderTagLabel(
    panel: CustomPanelAPI,
    text: String,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
    textColor: Color? = null,
    alignment: Alignment? = null,
    highlightTokens: List<String> = emptyList(),
) {
    val element = panel.createUIElement(width, height, false)
    val label = element.addAgcText(text, 0f, textColor ?: CampaignGuiStyle.DEFAULT_TEXT_COLOUR)
    val highlights = highlightTokens.filter { text.contains(it) }
    if (highlights.isNotEmpty()) {
        label.setHighlight(*highlights.toTypedArray())
        label.setHighlightColors(*Array(highlights.size) { CampaignGuiStyle.MODIFIER_TEXT_COLOUR })
    }
    alignment?.let(label::setAlignment)
    panel.addUIElement(element).inTL(x, y)
}

/**
 * Shared label-centering path for AGC-owned button overlays. The fitter decides
 * what text can fit, while LabelAPI alignment handles centering inside the true
 * highlighted/control region. This avoids tuning x-offsets by eye per button.
 */
fun renderCenteredControlLabel(
    panel: CustomPanelAPI,
    text: String,
    width: Float,
    height: Float,
    top: Float = CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
    centerRegionOffsetX: Float = 0f,
    centerRegionWidth: Float = width,
    horizontalPadding: Float = CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING,
    approxCharWidthPx: Float = CampaignGuiStyle.CENTERED_LABEL_CHAR_WIDTH_ESTIMATE,
    textColor: Color? = null,
    highlightTokens: List<String> = hiddenChangeHighlightTokens(text),
) {
    val fitted = computeTextFitLayout(
        text = text,
        availableWidth = centerRegionWidth,
        availableHeight = height,
        minRowHeight = height,
        horizontalPadding = 2f * horizontalPadding,
        verticalPadding = 0f,
        approxCharWidthPx = approxCharWidthPx,
        maxLines = 1,
        canGrowHeight = false
    )
    // Negative checkbox offsets expand the glow left of the panel; labels need
    // the clipped visible region or they read left-biased in game.
    val visibleRegionLeft = max(0f, centerRegionOffsetX)
    val left = visibleRegionLeft + horizontalPadding
    val renderWidth = min(
        centerRegionWidth - 2f * horizontalPadding,
        width - left - horizontalPadding
    ).coerceAtLeast(16f)
    renderTagLabel(
        panel = panel,
        text = fitted.wrappedText,
        width = renderWidth,
        height = height,
        x = left,
        y = top,
        textColor = textColor,
        alignment = Alignment.MID,
        highlightTokens = hiddenChangeHighlightTokens(fitted.wrappedText, highlightTokens)
    )
}

fun addCampaignPanelHeading(
    panel: CustomPanelAPI,
    title: String,
    top: Float = CampaignGuiStyle.PANEL_PADDING,
    horizontalPadding: Float = CampaignGuiStyle.PANEL_PADDING,
    fillColor: Color = CampaignGuiStyle.PANEL_HEADING_COLOUR,
    textColor: Color? = null,
    headingHeight: Float = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT,
): CustomPanelAPI {
    val headerPanel = panel.createCustomPanel(
        panel.position.width - 2f * horizontalPadding,
        headingHeight,
        CampaignPanelPlugin(CampaignPanelType.PANEL_HEADING, fillColor = fillColor)
    )
    panel.addComponent(headerPanel)
    headerPanel.position.inTL(horizontalPadding, top)

    val estimatedLabelWidth = title.length * CampaignGuiStyle.HEADING_CHAR_WIDTH_ESTIMATE
    val left = ((headerPanel.position.width - estimatedLabelWidth) / 2f).coerceAtLeast(CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING)
    val width = (headerPanel.position.width - left - CampaignGuiStyle.ITEM_TEXT_HORIZONTAL_PADDING).coerceAtLeast(16f)
    renderTagLabel(
        headerPanel,
        title,
        width,
        headingHeight - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
        left,
        CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
        textColor = textColor
    )
    return headerPanel
}

fun addCollapsibleCampaignPanelHeading(
    panel: CustomPanelAPI,
    title: String,
    collapsed: Boolean,
    data: Any,
    top: Float = CampaignGuiStyle.PANEL_PADDING,
    fillColor: Color = CampaignGuiStyle.COLLAPSIBLE_HEADING_COLOUR,
    textColor: Color? = null,
    headingHeight: Float = CampaignGuiStyle.CONTAINER_HEADING_HEIGHT,
    statusSuffix: String = "",
): StyledCampaignButtonShell {
    val headerWidth = panel.position.width - 2f * CampaignGuiStyle.PANEL_PADDING
    val headerPanel = panel.createCustomPanel(
        headerWidth,
        headingHeight,
        CampaignPanelPlugin(CampaignPanelType.PANEL_HEADING, fillColor = fillColor)
    )
    panel.addComponent(headerPanel)
    headerPanel.position.inTL(CampaignGuiStyle.PANEL_PADDING, top)

    val element = headerPanel.createUIElement(headerWidth, headingHeight, false)
    val colors = CampaignGuiStyle.checkboxColorsForButton(CampaignGuiStyle.sameColorButtonState(fillColor))
    val button = element.addAreaCheckbox(
        "",
        data,
        colors.base,
        colors.bg,
        colors.bright,
        headerWidth,
        headingHeight,
        0f
    )
    CampaignButtonSuppression.registerCampaignButton(button)
    element.addTooltipToPrevious(
        AGCGUI.makeTooltip("${if (collapsed) "Expand" else "Collapse"} $title panel."),
        TooltipLocation.BELOW
    )
    headerPanel.addUIElement(element).inTL(CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET, 0f)
    renderCenteredControlLabel(
        panel = headerPanel,
        text = "$title (${if (collapsed) "+" else "-"})$statusSuffix",
        width = headerWidth,
        height = headingHeight - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
        centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET,
        centerRegionWidth = headerWidth,
        textColor = textColor,
    )
    return StyledCampaignButtonShell(headerPanel, button)
}
}
