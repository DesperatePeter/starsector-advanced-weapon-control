package com.dp.advancedgunnerycontrol.io

import com.dp.advancedgunnerycontrol.config.Values
import com.fs.starfarer.api.Global
import org.json.JSONArray
import org.json.JSONObject
import org.lazywizard.lazylib.JSONUtils

fun clearJsonMapFile(file: String){
    try {
        val data = JSONUtils.loadCommonJSON(file)
        clearJsonObject(data)
        data.save()
    } catch (ex: Throwable) {
        logJsonWarn("Failed clearing common JSON map '$file'.", ex)
    }
}

fun saveJsonMapAsFile(file: String, map: Map<String, List<String>>){
    try {
        val data = JSONUtils.loadCommonJSON(file)
        clearJsonObject(data)
        map.forEach {
            data.put(it.key, JSONArray(it.value.toSet().toList()))
        }
        data.save()
    } catch (ex: Throwable) {
        logJsonWarn("Failed saving common JSON map '$file'.", ex)
    }
}

fun readJsonMapFromFile(file:String): Map<String, List<String>>{
    return try {
        val data = JSONUtils.loadCommonJSON(file)
        val valuesByKey = mutableMapOf <String, List<String>>()
        data.keys().forEach { key ->
            (key as? String)?.let {
                val array = data.optJSONArray(it)
                if (array == null) {
                    logJsonWarn("Ignoring malformed common JSON map entry '$it' in '$file' because it is not an array.")
                    return@forEach
                }
                valuesByKey[it] = jsonArrayToStringList(array)
            }
        }
        valuesByKey
    } catch (ex: Throwable) {
        logJsonWarn("Failed reading common JSON map '$file'. Falling back to empty data.", ex)
        emptyMap()
    }
}

private fun clearJsonObject(data: JSONObject) {
    val keys = mutableListOf<String>()
    data.keys().forEach { key ->
        (key as? String)?.let { keys.add(it) }
    }
    keys.forEach { data.remove(it) }
}

private fun jsonArrayToStringList(jsonArray: JSONArray): List<String> {
    val values = mutableListOf<String>()
    for (index in 0 until jsonArray.length()) {
        val value = jsonArray.opt(index) as? String ?: continue
        val trimmed = value.trim()
        if (trimmed.isNotEmpty()) values.add(trimmed)
    }
    return values
}

private fun logJsonWarn(message: String, throwable: Throwable? = null) {
    val logger = Global.getLogger(Values::class.java)
    if (throwable == null) logger.warn(message) else logger.warn(message, throwable)
}
