package com.dp.advancedgunnerycontrol.gui.suggesttaggui

import com.dp.advancedgunnerycontrol.settings.Settings
import com.dp.advancedgunnerycontrol.typesandvalues.Values
import org.json.JSONArray
import org.lazywizard.lazylib.JSONUtils
import org.magiclib.kotlin.toStringList

fun backupSuggestedTagsToJson(){
    clearSuggestedTagsToJson()
    val data = JSONUtils.loadCommonJSON(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME)
    Settings.getCurrentSuggestedTags().forEach {
        data.put(it.key, it.value.toSet().toList())
    }
    data.save()
}

fun restoreSuggestedTagsFromJson(){
    val data = JSONUtils.loadCommonJSON(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME)
    Settings.customSuggestedTags = emptyMap()
    val m = mutableMapOf <String, List<String>>()
    data.keys().forEach { key ->
        (key as? String)?.let {
            m[key] = (data.get(key) as JSONArray).toStringList()
        }
    }
    Settings.customSuggestedTags = m
}

fun clearSuggestedTagsToJson(){
    val data = JSONUtils.loadCommonJSON(Values.CUSTOM_SUGGESTED_TAG_JSON_FILE_NAME)
    val keys = mutableListOf<String>()
    data.keys().forEach {k->
        (k as? String)?.let { keys.add(it) }
    }
    keys.forEach {
        data.remove(it)
    }
    data.save()
}