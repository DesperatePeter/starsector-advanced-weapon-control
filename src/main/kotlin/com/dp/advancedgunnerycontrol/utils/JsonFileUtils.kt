package com.dp.advancedgunnerycontrol.utils

import com.dp.advancedgunnerycontrol.typesandvalues.Values
import org.json.JSONArray
import org.lazywizard.lazylib.JSONUtils
import org.magiclib.kotlin.toStringList

fun clearJsonMapFile(file: String){
    val data = JSONUtils.loadCommonJSON(file)
    val keys = mutableListOf<String>()
    data.keys().forEach {k->
        (k as? String)?.let { keys.add(it) }
    }
    keys.forEach {
        data.remove(it)
    }
    data.save()
}

fun saveJsonMapAsFile(file: String, map: Map<String, List<String>>){
    clearJsonMapFile(file)
    val data = JSONUtils.loadCommonJSON(file)
    map.forEach{
        data.put(it.key, it.value.toSet().toList())
    }
    data.save()
}

fun readJsonMapFromFile(file:String): Map<String, List<String>>{
    val data = JSONUtils.loadCommonJSON(file)
    val m = mutableMapOf <String, List<String>>()
    data.keys().forEach { key ->
        (key as? String)?.let {
            m[key] = (data.get(key) as JSONArray).toStringList()
        }
    }
    return m
}