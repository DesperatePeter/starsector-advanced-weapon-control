package com.dp.advancedgunnerycontrol.gui.modals

import com.dp.advancedgunnerycontrol.gui.controls.rows.CampaignActionRows
import com.dp.advancedgunnerycontrol.gui.controls.suppression.CampaignButtonSuppression
import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import org.lwjgl.input.Keyboard

enum class CampaignConfirmationTone {
    CAUTION,
    WARNING,
}

data class CampaignTextHighlight(
    val token: String,
    val color: Color,
)

data class CampaignHighlightedText(
    val text: String,
    val highlights: List<CampaignTextHighlight> = emptyList(),
)

data class CampaignConfirmationModalRequest(
    val title: String,
    val body: String,
    val richBody: List<CampaignHighlightedText> = emptyList(),
    val tone: CampaignConfirmationTone = CampaignConfirmationTone.CAUTION,
    val onConfirm: () -> Unit,
    val onCancel: () -> Unit,
)

data class RenderedCampaignConfirmationModal(
    val backdrop: CustomPanelAPI,
    val dialog: CustomPanelAPI,
    val dialogPosition: PositionAPI,
    val backdropButtons: List<ButtonAPI>,
    val confirmButton: ButtonAPI,
    val cancelButton: ButtonAPI,
)

fun processCampaignConfirmationModalInput(
    events: MutableList<InputEventAPI>?,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onMouseEventBeforeConsume: (InputEventAPI) -> Boolean = { false },
): Boolean {
    events?.forEach { event ->
        if (event.isConsumed) return@forEach
        if (event.isKeyDownEvent) {
            when (event.eventValue) {
                Keyboard.KEY_ESCAPE -> {
                    event.consume()
                    onCancel()
                }
                Keyboard.KEY_SPACE -> {
                    event.consume()
                    onConfirm()
                }
                else -> event.consume()
            }
            return@forEach
        }
        if (event.isMouseEvent) {
            if (onMouseEventBeforeConsume(event)) return@forEach
            event.consume()
        }
    }
    return true
}

data class CampaignModalBodyParagraph(
    val paragraph: CampaignHighlightedText,
    val layout: WrappedLabelLayout,
    val pad: Float,
)

private const val CONFIRMATION_MODAL_WIDTH = CampaignGuiStyle.EDIT_TAG_MODAL_WIDTH
private const val CONFIRMATION_MODAL_PADDING = CampaignGuiStyle.MODAL_PADDING
private const val CONFIRMATION_MODAL_FOOTER_HORIZONTAL_INSET = CampaignGuiStyle.MODAL_FOOTER_HORIZONTAL_INSET
private const val CONFIRMATION_MODAL_BUTTON_WIDTH = CampaignGuiStyle.MODAL_BUTTON_WIDTH
private const val CONFIRMATION_MODAL_BUTTON_HEIGHT = CampaignGuiStyle.MODAL_ROW_HEIGHT
private const val CONFIRMATION_MODAL_HEADING_HEIGHT = CampaignGuiStyle.MODAL_HEADING_HEIGHT
private const val CONFIRMATION_MODAL_TITLE_BODY_GAP = CampaignGuiStyle.MODAL_TITLE_BODY_GAP
private const val CONFIRMATION_MODAL_BODY_ACTION_GAP = CampaignGuiStyle.MODAL_BODY_ACTION_GAP
private const val CONFIRMATION_MODAL_BODY_PARAGRAPH_GAP = CampaignGuiStyle.MODAL_TEXT_COMPONENT_GAP
private const val CONFIRMATION_MODAL_BODY_MAX_LINES = 80
private val WARNING_TITLE_COLOR = CampaignGuiStyle.ALERT_RED_COLOR

private data class CampaignConfirmationLayout(
    val dialogWidth: Float,
    val dialogHeight: Float,
    val contentWidth: Float,
    val bodyHeight: Float,
    val bodyScrollable: Boolean,
    val bodyParagraphs: List<CampaignModalBodyParagraph>,
)

fun confirmationFooterText(): CampaignHighlightedText {
    return CampaignHighlightedText(
        "Press Spacebar or Confirm to continue. Press Escape or Cancel to abort.",
        listOf(
            CampaignTextHighlight("Spacebar", CampaignGuiStyle.CONFIRM_BUTTON_HOVER_COLOR),
            CampaignTextHighlight("Confirm", CampaignGuiStyle.CONFIRM_BUTTON_HOVER_COLOR),
            CampaignTextHighlight("Escape", CampaignGuiStyle.CANCEL_BUTTON_HOVER_COLOR),
            CampaignTextHighlight("Cancel", CampaignGuiStyle.CANCEL_BUTTON_HOVER_COLOR),
        )
    )
}

fun confirmationBodyWithFooter(text: String): List<CampaignHighlightedText> {
    return listOf(
        CampaignHighlightedText(text),
        confirmationFooterText()
    )
}

fun warningBodyWithFooter(text: String): List<CampaignHighlightedText> {
    return listOf(
        CampaignHighlightedText(
            text,
            listOf(CampaignTextHighlight(text, CampaignGuiStyle.ALERT_RED_COLOR))
        ),
        confirmationFooterText()
    )
}

fun confirmationBodyForTone(text: String, tone: CampaignConfirmationTone): List<CampaignHighlightedText> {
    return when (tone) {
        CampaignConfirmationTone.WARNING -> warningBodyWithFooter(text)
        CampaignConfirmationTone.CAUTION -> confirmationBodyWithFooter(text)
    }
}

fun campaignConfirmationRequestWithFooter(
    title: String,
    description: String,
    tone: CampaignConfirmationTone,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
): CampaignConfirmationModalRequest {
    return CampaignConfirmationModalRequest(
        title = title,
        body = "",
        richBody = confirmationBodyForTone(description, tone),
        tone = tone,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

/**
 * Generic confirmation/warning popup component.
 * Renders the shared modal shell with title, body text, and Confirm/Cancel buttons.
 */
fun renderCampaignConfirmationModal(
    root: CustomPanelAPI,
    request: CampaignConfirmationModalRequest,
    confirmData: Any,
    cancelData: Any,
    backdropData: Any,
): RenderedCampaignConfirmationModal {
    val accentColor = when (request.tone) {
        CampaignConfirmationTone.WARNING -> CampaignGuiStyle.CANCEL_BUTTON_HOVER_COLOR
        CampaignConfirmationTone.CAUTION -> CampaignGuiStyle.LOAD_BUTTON_HOVER_COLOR
    }
    val titleColor = when (request.tone) {
        CampaignConfirmationTone.WARNING -> WARNING_TITLE_COLOR
        CampaignConfirmationTone.CAUTION -> CampaignGuiStyle.LOAD_BUTTON_HOVER_COLOR
    }

    val backdrop = root.createCustomPanel(
        root.position.width,
        root.position.height,
        CampaignPanelPlugin(CampaignPanelType.WEAPON_GROUPS_PANEL, fillColor = CampaignGuiStyle.MODAL_BACKDROP_FILL_COLOR)
    )
    root.addComponent(backdrop)
    backdrop.position.inTL(0f, 0f)

    val layout = computeCampaignConfirmationLayout(root, request)
    val dialogWidth = layout.dialogWidth
    val dialogHeight = layout.dialogHeight
    val dialogX = max(CampaignGuiStyle.PANEL_PADDING, (root.position.width - dialogWidth) / 2f)
    val dialogY = max(CampaignGuiStyle.PANEL_PADDING, (root.position.height - dialogHeight) / 2f)
    val backdropButtons = addModalBackdropInputShields(backdrop, backdropData, dialogX, dialogY, dialogWidth, dialogHeight)
    val dialog = root.createCustomPanel(
        dialogWidth,
        dialogHeight,
        CampaignPanelPlugin(
            CampaignPanelType.CONTROL_PANEL,
            lineWidth = 2f,
            fillColor = CampaignGuiStyle.MODAL_DIALOG_FILL_COLOR,
            borderColor = accentColor
        )
    )
    root.addComponent(dialog)
    dialog.position.inTL(dialogX, dialogY)

    val heading = dialog.createUIElement(layout.contentWidth, CONFIRMATION_MODAL_HEADING_HEIGHT, false)
    // Raw setParaFont("graphics/fonts/...") overrides have crashed here.
    // Use Starsector's built-in font selectors instead of direct font paths.
    heading.addAgcLargeHeading(request.title, titleColor)
    dialog.addUIElement(heading).inTL(CONFIRMATION_MODAL_PADDING, CONFIRMATION_MODAL_PADDING)

    val bodyTop = CONFIRMATION_MODAL_PADDING + CONFIRMATION_MODAL_HEADING_HEIGHT + CONFIRMATION_MODAL_TITLE_BODY_GAP
    val body = dialog.createUIElement(layout.contentWidth, layout.bodyHeight, layout.bodyScrollable)
    body.setParaFontDefault()
    renderCampaignHighlightedTextBody(body, layout.bodyParagraphs)
    dialog.addUIElement(body).inTL(CONFIRMATION_MODAL_PADDING, bodyTop)

    val buttonTop = bodyTop + layout.bodyHeight + CONFIRMATION_MODAL_BODY_ACTION_GAP
    val confirmButton = addModalButton(
        dialog = dialog,
        data = confirmData,
        x = CONFIRMATION_MODAL_PADDING + CONFIRMATION_MODAL_FOOTER_HORIZONTAL_INSET,
        y = buttonTop,
        text = "Confirm",
        colors = CampaignGuiStyle.CONFIRM_BUTTON_COLORS
    )
    val cancelButton = addModalButton(
        dialog = dialog,
        data = cancelData,
        x = dialogWidth -
            CONFIRMATION_MODAL_PADDING -
            CONFIRMATION_MODAL_FOOTER_HORIZONTAL_INSET -
            CONFIRMATION_MODAL_BUTTON_WIDTH,
        y = buttonTop,
        text = "Cancel",
        colors = CampaignGuiStyle.CANCEL_BUTTON_COLORS
    )

    return RenderedCampaignConfirmationModal(
        backdrop = backdrop,
        dialog = dialog,
        dialogPosition = dialog.position,
        backdropButtons = backdropButtons,
        confirmButton = confirmButton,
        cancelButton = cancelButton,
    )
}

private fun computeCampaignConfirmationLayout(
    root: CustomPanelAPI,
    request: CampaignConfirmationModalRequest,
): CampaignConfirmationLayout {
    val dialogWidth = min(CONFIRMATION_MODAL_WIDTH, root.position.width - 2f * CampaignGuiStyle.PANEL_PADDING)
    val contentWidth = dialogWidth - 2f * CONFIRMATION_MODAL_PADDING
    val bodyParagraphs = campaignModalBodyParagraphs(
        paragraphs = request.richBody.takeIf { it.isNotEmpty() }
            ?: listOf(CampaignHighlightedText(request.body)),
        contentWidth = contentWidth,
        minRowHeight = CONFIRMATION_MODAL_BUTTON_HEIGHT,
        paragraphGap = CONFIRMATION_MODAL_BODY_PARAGRAPH_GAP,
        maxLines = CONFIRMATION_MODAL_BODY_MAX_LINES,
    )
    val fullBodyHeight = campaignModalBodyHeight(bodyParagraphs)
        .coerceAtLeast(CONFIRMATION_MODAL_BUTTON_HEIGHT)
    val desiredHeight = CampaignGuiStyle.modalHeightForBody(
        bodyHeight = fullBodyHeight,
        headingHeight = CONFIRMATION_MODAL_HEADING_HEIGHT,
        footerHeight = CONFIRMATION_MODAL_BUTTON_HEIGHT,
        titleBodyGap = CONFIRMATION_MODAL_TITLE_BODY_GAP,
        bodyActionGap = CONFIRMATION_MODAL_BODY_ACTION_GAP,
    )
    val dialogHeight = min(desiredHeight, maxCampaignConfirmationModalHeight(root.position.height))
    val bodyHeight = min(
        fullBodyHeight,
        CampaignGuiStyle.modalBodyHeightForDialog(
            dialogHeight = dialogHeight,
            headingHeight = CONFIRMATION_MODAL_HEADING_HEIGHT,
            footerHeight = CONFIRMATION_MODAL_BUTTON_HEIGHT,
            minimumBodyHeight = CONFIRMATION_MODAL_BUTTON_HEIGHT,
            titleBodyGap = CONFIRMATION_MODAL_TITLE_BODY_GAP,
            bodyActionGap = CONFIRMATION_MODAL_BODY_ACTION_GAP,
        )
    )
    return CampaignConfirmationLayout(
        dialogWidth = dialogWidth,
        dialogHeight = dialogHeight,
        contentWidth = contentWidth,
        bodyHeight = bodyHeight,
        bodyScrollable = fullBodyHeight > bodyHeight + 0.5f,
        bodyParagraphs = bodyParagraphs,
    )
}

private fun maxCampaignConfirmationModalHeight(screenHeight: Float): Float {
    return CampaignGuiStyle.maxModalHeight(screenHeight, CONFIRMATION_MODAL_BUTTON_HEIGHT)
}

fun campaignModalBodyParagraphs(
    paragraphs: List<CampaignHighlightedText>,
    contentWidth: Float,
    minRowHeight: Float,
    paragraphGap: Float,
    maxLines: Int,
): List<CampaignModalBodyParagraph> {
    return paragraphs.mapIndexed { index, paragraph ->
        val pad = if (index == 0) 0f else paragraphGap
        CampaignModalBodyParagraph(
            paragraph = paragraph,
            pad = pad,
            layout = computeTextFitLayout(
                text = paragraph.text,
                availableWidth = contentWidth,
                minRowHeight = minRowHeight,
                horizontalPadding = 0f,
                verticalPadding = 0f,
                approxCharWidthPx = CampaignGuiStyle.ACTION_LABEL_APPROX_CHAR_WIDTH,
                lineHeightPx = CampaignGuiStyle.MODAL_BODY_LINE_HEIGHT,
                maxLines = maxLines,
            )
        )
    }
}

fun campaignModalBodyHeight(paragraphs: List<CampaignModalBodyParagraph>): Float {
    return paragraphs.sumOf { (it.pad + it.layout.renderHeight).toDouble() }.toFloat()
}

fun renderCampaignHighlightedTextBody(
    body: TooltipMakerAPI,
    paragraphs: List<CampaignModalBodyParagraph>,
) {
    paragraphs.forEach { bodyParagraph ->
        val paragraph = bodyParagraph.paragraph
        val layoutText = bodyParagraph.layout.wrappedText
        val fullParagraphHighlight = paragraph.highlights.singleOrNull { it.token == paragraph.text }
        val label = body.addAgcText(
            layoutText,
            bodyParagraph.pad,
            fullParagraphHighlight?.color ?: CampaignGuiStyle.DEFAULT_TEXT_COLOUR
        )
        val highlights = paragraph.highlights
            .filter { it !== fullParagraphHighlight }
            .mapNotNull { highlight ->
                val token = when {
                    layoutText.contains(highlight.token) -> highlight.token
                    highlight.token == paragraph.text && layoutText.isNotBlank() -> layoutText
                    else -> null
                }
                token?.let { CampaignTextHighlight(it, highlight.color) }
            }
            .distinctBy { it.token }
        if (highlights.isNotEmpty()) {
            label.setHighlight(*highlights.map { it.token }.toTypedArray())
            label.setHighlightColors(*highlights.map { it.color }.toTypedArray())
        }
    }
}

private fun addModalButton(
    dialog: CustomPanelAPI,
    data: Any,
    x: Float,
    y: Float,
    text: String,
    colors: CampaignGuiStyle.ButtonStateColors,
): ButtonAPI {
    val shell = CampaignActionRows.addStyledCampaignButtonShell(
        parent = dialog,
        data = data,
        x = x,
        y = y,
        width = CONFIRMATION_MODAL_BUTTON_WIDTH,
        height = CONFIRMATION_MODAL_BUTTON_HEIGHT,
        colors = colors,
        fillIdle = true
    )
    CampaignControlLabels.renderCenteredControlLabel(
        panel = shell.panel,
        text = text,
        width = CONFIRMATION_MODAL_BUTTON_WIDTH,
        height = CONFIRMATION_MODAL_BUTTON_HEIGHT - CampaignGuiStyle.ITEM_TEXT_TOP_PADDING,
        centerRegionOffsetX = CampaignGuiStyle.ITEM_HIGHLIGHT_X_OFFSET
    )
    return shell.button
}

private fun addModalBackdropInputShields(
    backdrop: CustomPanelAPI,
    backdropData: Any,
    dialogX: Float,
    dialogY: Float,
    dialogWidth: Float,
    dialogHeight: Float,
): List<ButtonAPI> {
    val rootWidth = backdrop.position.width
    val rootHeight = backdrop.position.height
    return listOfNotNull(
        addModalBackdropInputShield(backdrop, backdropData, 0f, 0f, rootWidth, dialogY),
        addModalBackdropInputShield(backdrop, backdropData, 0f, dialogY + dialogHeight, rootWidth, rootHeight - dialogY - dialogHeight),
        addModalBackdropInputShield(backdrop, backdropData, 0f, dialogY, dialogX, dialogHeight),
        addModalBackdropInputShield(backdrop, backdropData, dialogX + dialogWidth, dialogY, rootWidth - dialogX - dialogWidth, dialogHeight),
    )
}

private fun addModalBackdropInputShield(
    backdrop: CustomPanelAPI,
    backdropData: Any,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
): ButtonAPI? {
    // Outside-only click zones avoid the old full-screen backdrop bug where
    // clicks inside the dialog could be interpreted as click-away cancellation.
    return addTransparentCampaignInputShield(backdrop, backdropData, x, y, width, height)
}

internal fun addTransparentCampaignInputShield(
    parent: CustomPanelAPI,
    data: Any,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
): ButtonAPI? {
    if (width <= 1f || height <= 1f) return null
    val shield = parent.createUIElement(width, height, false)
    val button = shield.addAreaCheckbox(
        "",
        data,
        CampaignGuiStyle.TRANSPARENT_PANEL_FILL_COLOR,
        CampaignGuiStyle.TRANSPARENT_PANEL_FILL_COLOR,
        CampaignGuiStyle.TRANSPARENT_PANEL_FILL_COLOR,
        width,
        height,
        0f
    )
    CampaignButtonSuppression.registerCampaignButton(button)
    muteCampaignButtonSounds(button)
    button.setShowTooltipWhileInactive(false)
    parent.addUIElement(shield).inTL(x, y)
    return button
}

fun muteCampaignButtonSounds(button: ButtonAPI) {
    // Empty strings crash as missing sound ids; null is the Starsector
    // convention for suppressing a button sound.
    button.setMouseOverSound(null)
    button.setButtonPressedSound(null)
    button.setButtonDisabledPressedSound(null)
}

fun eventIsInsideCampaignConfirmationModal(dialogPosition: PositionAPI?, event: InputEventAPI): Boolean {
    if (event.isConsumed) return false
    val position = dialogPosition ?: return false
    if (position.containsEvent(event)) return true

    val x = event.x.toFloat()
    val y = event.y.toFloat()
    return x >= position.x &&
        x <= position.x + position.width &&
        y >= position.y &&
        y <= position.y + position.height
}
