package com.dp.advancedgunnerycontrol.gui.panels.content

import com.dp.advancedgunnerycontrol.gui.controls.text.CampaignControlLabels

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.foundation.*

import com.dp.advancedgunnerycontrol.gui.*


import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI

private object CampaignContentPanelsLog

fun campaignRootContentWidth(
    root: CustomPanelAPI,
    minimum: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_WIDTH,
): Float = boundedContentSize(root.position.width, minimum)

fun campaignRootContentHeight(
    root: CustomPanelAPI,
    minimum: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_HEIGHT,
): Float = boundedContentSize(root.position.height, minimum)

private fun boundedContentSize(parentSize: Float, minimum: Float): Float {
    val available = (parentSize - 2f * CampaignGuiStyle.MAIN_PADDING).coerceAtLeast(0f)
    return available.coerceAtLeast(minimum).coerceAtMost(available)
}

fun addCampaignRootContentPanel(
    root: CustomPanelAPI,
    plugin: CustomUIPanelPlugin,
    minimumWidth: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_WIDTH,
    minimumHeight: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_HEIGHT,
): CustomPanelAPI {
    val content = root.createCustomPanel(
        campaignRootContentWidth(root, minimumWidth),
        campaignRootContentHeight(root, minimumHeight),
        plugin
    )
    root.addComponent(content).inTL(CampaignGuiStyle.MAIN_PADDING, CampaignGuiStyle.MAIN_PADDING)
    return content
}

fun addCampaignErrorFallbackPanel(
    root: CustomPanelAPI,
    title: String,
    message: String,
    reason: String,
    highlightToken: String,
): CustomPanelAPI {
    val width = campaignRootContentWidth(root, CampaignGuiStyle.ERROR_FALLBACK_MIN_WIDTH)
    val height = CampaignGuiStyle.ERROR_FALLBACK_HEIGHT
    val fallback = root.createCustomPanel(
        width,
        height,
        CampaignPanelPlugin(CampaignPanelType.OPTIONS_PANEL)
    )
    root.addComponent(fallback).inTL(CampaignGuiStyle.MAIN_PADDING, CampaignGuiStyle.MAIN_PADDING)

    val padding = CampaignGuiStyle.ERROR_FALLBACK_PADDING
    CampaignControlLabels.addCampaignPanelHeading(
        panel = fallback,
        title = title,
        top = padding,
        horizontalPadding = padding,
    )
    val bodyTop = padding + CampaignGuiStyle.CONTAINER_HEADING_HEIGHT + CampaignGuiStyle.MODAL_TITLE_BODY_GAP
    val element = fallback.createUIElement(width - 2f * padding, height - bodyTop - padding, false)
    element.addAgcHighlightedText(
        message,
        0f,
        CampaignGuiStyle.CANCEL_BUTTON_HOVER_COLOR,
        highlightToken
    )
    element.addAgcText(reason, 6f)
    fallback.addUIElement(element).inTL(padding, bodyTop)
    return fallback
}

fun clearCampaignContentPanel(
    root: CustomPanelAPI,
    content: CustomPanelAPI?,
    onCleared: () -> Unit,
) {
    try {
        content?.let { panel ->
            runCatching { root.removeComponent(panel) }
                .onFailure { ex ->
                    Global.getLogger(CampaignContentPanelsLog::class.java)
                        .warn("[AGC_GUI] Failed to remove campaign content panel", ex)
                }
        }
    } finally {
        onCleared()
    }
}

fun replaceCampaignRootContentPanel(
    root: CustomPanelAPI,
    content: CustomPanelAPI?,
    plugin: CustomUIPanelPlugin,
    onCleared: () -> Unit,
    minimumWidth: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_WIDTH,
    minimumHeight: Float = CampaignGuiStyle.ROOT_CONTENT_MIN_HEIGHT,
): CustomPanelAPI {
    clearCampaignContentPanel(root, content, onCleared)
    return addCampaignRootContentPanel(
        root = root,
        plugin = plugin,
        minimumWidth = minimumWidth,
        minimumHeight = minimumHeight,
    )
}

fun replaceCampaignContentWithErrorFallback(
    root: CustomPanelAPI,
    content: CustomPanelAPI?,
    title: String,
    message: String,
    reason: String,
    highlightToken: String,
    onCleared: () -> Unit,
): CustomPanelAPI {
    clearCampaignContentPanel(root, content, onCleared)
    return addCampaignErrorFallbackPanel(
        root = root,
        title = title,
        message = message,
        reason = reason,
        highlightToken = highlightToken
    )
}
