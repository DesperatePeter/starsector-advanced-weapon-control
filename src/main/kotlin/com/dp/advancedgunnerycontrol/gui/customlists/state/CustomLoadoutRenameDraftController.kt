package com.dp.advancedgunnerycontrol.gui.customlists.state

internal class CustomLoadoutRenameDraftController(
    private val draftStore: CustomListDraftStore,
) {
    fun loadoutRenameIndex(defaultIndex: Int, maxLoadouts: Int): Int {
        return (draftStore.value(CustomListDraftKeys.LoadoutRename.INDEX)?.toIntOrNull() ?: defaultIndex)
            .coerceIn(0, maxLoadouts.coerceAtLeast(1) - 1)
    }

    fun loadoutRenameDraft(defaultIndex: Int, maxLoadouts: Int, displayName: (Int) -> String): String {
        val index = loadoutRenameIndex(defaultIndex, maxLoadouts)
        return draftStore.value(CustomListDraftKeys.LoadoutRename.NAME) ?: displayName(index)
    }

    fun setLoadoutRenameDraft(index: Int, name: String) {
        val values = draftStore.values()
        values[CustomListDraftKeys.LoadoutRename.INDEX] = index.toString()
        values[CustomListDraftKeys.LoadoutRename.NAME] = name.take(24)
        draftStore.writeValues(values)
    }
}
