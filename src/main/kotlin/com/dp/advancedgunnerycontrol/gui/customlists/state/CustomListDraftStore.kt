package com.dp.advancedgunnerycontrol.gui.customlists.state

internal class CustomListDraftStore(
    private val valuesProvider: (() -> Map<String, String>)?,
    private val valuesUpdater: ((Map<String, String>) -> Unit)?,
    private val onChanged: () -> Unit,
) {
    fun values(): MutableMap<String, String> =
        valuesProvider?.invoke()?.toMutableMap() ?: mutableMapOf()

    fun value(key: String): String? {
        return valuesProvider?.invoke()?.get(key)
    }

    fun valueList(key: String): List<String> {
        return value(key)
            ?.split(CustomListDraftKeys.STATE_SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }

    fun toggleSet(key: String, value: String) {
        val expanded = valueList(key).toMutableSet()
        if (!expanded.add(value)) expanded.remove(value)
        writeValue(key, expanded.joinToString(CustomListDraftKeys.STATE_SEPARATOR))
    }

    fun canonicalList(
        key: String,
        canonicalize: (String) -> String,
    ): List<String> =
        valueList(key)
            .map(canonicalize)
            .filter { it.isNotBlank() }
            .distinct()

    fun writeCanonicalList(
        key: String,
        values: List<String>,
        canonicalize: (String) -> String,
    ) {
        writeValue(
            key,
            values
                .map(canonicalize)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(CustomListDraftKeys.STATE_SEPARATOR),
        )
    }

    fun canonicalEditMap(
        key: String,
        canonicalize: (String) -> String,
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        valueList(key).forEach { pair ->
            val split = pair.split(CustomListDraftKeys.EDIT_PAIR_SEPARATOR, limit = 2)
            if (split.size != 2) return@forEach
            val source = canonicalize(split[0])
            val edited = canonicalize(split[1])
            if (source.isNotBlank() && edited.isNotBlank()) {
                result[source] = edited
            }
        }
        return result
    }

    fun writeCanonicalEditMap(
        key: String,
        edits: Map<String, String>,
        canonicalize: (String) -> String,
    ) {
        val serialized = edits.entries
            .mapNotNull { (source, edited) ->
                val canonicalSource = canonicalize(source)
                val canonicalEdited = canonicalize(edited)
                if (
                    canonicalSource.isBlank() ||
                    canonicalEdited.isBlank() ||
                    canonicalSource == canonicalEdited
                ) {
                    null
                } else {
                    "$canonicalSource${CustomListDraftKeys.EDIT_PAIR_SEPARATOR}$canonicalEdited"
                }
            }
            .joinToString(CustomListDraftKeys.STATE_SEPARATOR)
        writeValue(key, serialized)
    }

    fun writeValue(key: String, value: String) {
        val values = values()
        values[key] = value
        writeValues(values)
    }

    fun writeValues(values: Map<String, String>) {
        valuesUpdater?.invoke(values)
        onChanged()
    }
}
