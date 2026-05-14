package com.dp.advancedgunnerycontrol.gui.options.rendering

import com.dp.advancedgunnerycontrol.gui.session.*

import com.dp.advancedgunnerycontrol.gui.style.*

import com.dp.advancedgunnerycontrol.gui.*

import com.dp.advancedgunnerycontrol.gui.layout.*
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionRow
import com.dp.advancedgunnerycontrol.gui.options.model.CampaignOptionsLayout

import com.dp.advancedgunnerycontrol.gui.actions.CycleTagListModeAction
import com.dp.advancedgunnerycontrol.gui.actions.NextShipAction
import com.dp.advancedgunnerycontrol.settings.Settings
import kotlin.math.max
import org.lwjgl.input.Keyboard

private data class CampaignOptionLayoutCacheKey(
    val width: Float,
    val minHeight: Float,
    val rows: List<CampaignOptionRowLayoutKey>,
)

private data class CampaignOptionRowLayoutKey(
    val label: String,
    val shortcut: Int?,
)

private data class CampaignOptionRowLayoutMetrics(
    val wrappedText: String,
    val rowHeight: Float,
)

private data class CachedCampaignOptionLayout(
    val key: CampaignOptionLayoutCacheKey,
    val metrics: List<CampaignOptionRowLayoutMetrics>,
    val requiredHeight: Float,
)

internal class CampaignOptionLayoutCache {
    companion object {
        private const val MAX_ENTRIES = 8
    }

    private val entries = LinkedHashMap<CampaignOptionLayoutCacheKey, CachedCampaignOptionLayout>()

    fun clear() {
        entries.clear()
    }

    fun layout(
        width: Float,
        rows: List<CampaignOptionRow>,
        minHeight: Float = 0f,
    ): CampaignOptionsLayout {
        val key = CampaignOptionLayoutCacheKey(
            width = width,
            minHeight = minHeight,
            rows = rows.map { row -> CampaignOptionRowLayoutKey(row.label, row.shortcut) },
        )
        entries[key]?.let { cached -> return cached.toLayout(rows) }

        val computed = CampaignOptionPanelLayout.computeCampaignOptionsLayout(width, rows, minHeight)
        val cached = CachedCampaignOptionLayout(
            key = key,
            metrics = computed.rows.map { row ->
                CampaignOptionRowLayoutMetrics(
                    wrappedText = row.wrappedText,
                    rowHeight = row.rowHeight,
                )
            },
            requiredHeight = computed.requiredHeight,
        )
        entries[key] = cached
        trimOldest()
        return computed
    }

    private fun CachedCampaignOptionLayout.toLayout(rows: List<CampaignOptionRow>): CampaignOptionsLayout {
        return CampaignOptionsLayout(
            rows = metrics.zip(rows).map { (metric, row) ->
                CampaignActionRowLayout(
                    row = row,
                    wrappedText = metric.wrappedText,
                    rowHeight = metric.rowHeight,
                )
            },
            requiredHeight = requiredHeight,
        )
    }

    private fun trimOldest() {
        while (entries.size > MAX_ENTRIES) {
            val oldest = entries.keys.firstOrNull() ?: return
            entries.remove(oldest)
        }
    }
}

object CampaignOptionPanelLayout {
    fun computeCampaignOptionsLayout(
        width: Float,
        rows: List<CampaignOptionRow>,
        minHeight: Float = 0f,
    ): CampaignOptionsLayout {
        val rowLayouts = computeCampaignActionRowLayouts(
            rows = rows,
            width = width,
            maxLines = CampaignGuiStyle.ACTION_LABEL_MAX_LINES,
            label = { row -> row.label },
            shortcuts = { row -> row.shortcut?.let(::listOf) ?: emptyList() },
        )
        val requiredHeight = max(
            minHeight,
            CampaignGuiStyle.PANEL_PADDING +
                CampaignGuiStyle.CONTAINER_HEADING_HEIGHT +
                campaignActionRowsHeight(rowLayouts) +
                CampaignGuiStyle.PANEL_PADDING
        )
        return CampaignOptionsLayout(
            rows = rowLayouts,
            requiredHeight = requiredHeight
        )
    }

    fun estimateCampaignOptionsHeight(
        width: Float,
        rows: List<CampaignOptionRow>,
        extraRowsProvider: () -> List<CampaignOptionRow> = { emptyList() },
        minHeight: Float = 0f,
    ): Float {
        return computeCampaignOptionsLayout(
            width = width,
            rows = campaignOptionRowsWithExtra(rows, extraRowsProvider()),
            minHeight = minHeight
        ).requiredHeight
    }

    fun estimateStableCampaignOptionsHeight(
        width: Float,
        capabilities: ShipEditorCapabilities,
        minHeight: Float = 0f,
    ): Float {
        return computeCampaignOptionsLayout(
            width = width,
            rows = stableAdvancedOptionRows(capabilities),
            minHeight = minHeight
        ).requiredHeight
    }

    fun campaignOptionRowsWithExtra(
        baseRows: List<CampaignOptionRow>,
        extraRows: List<CampaignOptionRow>,
    ): List<CampaignOptionRow> {
        if (extraRows.isEmpty()) return baseRows
        val rows = baseRows.toMutableList()
        val manageTagsIndex = rows.indexOfFirst { it.label == "Manage Tags and Ship Modes" || it.label == "Manage Tags" }
        val insertIndex = if (manageTagsIndex >= 0) {
            manageTagsIndex + 1
        } else {
            val toggleIndex = rows.indexOfFirst { it.label.startsWith("Switch to ") }
            if (toggleIndex >= 0) toggleIndex + 1 else rows.size
        }
        rows.addAll(insertIndex.coerceIn(0, rows.size), extraRows)
        return rows
    }

    fun stableAdvancedOptionRows(capabilities: ShipEditorCapabilities): List<CampaignOptionRow> {
        val rows = mutableListOf<CampaignOptionRow>()
        if (capabilities.canSelectOtherShips) {
            rows.add(stableLayoutRow(NextShipAction.STABLE_LAYOUT_NAME, Keyboard.KEY_TAB))
        }
        if (capabilities.canCycleLoadout) {
            rows.add(stableLayoutRow("Cycle loadout [1 / ${Settings.maxLoadouts()}] <Normal>"))
        }
        rows.add(stableLayoutRow(CycleTagListModeAction.STABLE_LAYOUT_NAME))
        if (capabilities.canCustomizeSuggestedTags) {
            rows.add(stableLayoutRow("Customize suggested tags"))
        }
        rows.add(stableLayoutRow("Switch to simple mode"))
        rows.add(stableLayoutRow("Manage Tags and Ship Modes", kind = CampaignActionButtonKind.SAVE))
        rows.add(stableLayoutRow("Save", kind = CampaignActionButtonKind.SAVE))
        rows.add(stableLayoutRow("Load", kind = CampaignActionButtonKind.LOAD))
        rows.add(stableLayoutRow("Reset \"All Ships\" List", kind = CampaignActionButtonKind.LOAD))
        rows.add(stableLayoutRow("Reset tags and ship modes", kind = CampaignActionButtonKind.LOAD))
        if (capabilities.canReloadSettings) {
            rows.add(stableLayoutRow("Reload Settings.EDITME", kind = CampaignActionButtonKind.LOAD))
        }
        return rows
    }

    private fun stableLayoutRow(
        label: String,
        shortcut: Int? = null,
        kind: CampaignActionButtonKind = CampaignActionButtonKind.UNCOLOURED,
    ): CampaignOptionRow {
        return CampaignOptionRow(
            label = label,
            tooltip = "",
            shortcut = shortcut,
            kind = kind,
            confirmationDescription = null,
            callback = {}
        )
    }
}
