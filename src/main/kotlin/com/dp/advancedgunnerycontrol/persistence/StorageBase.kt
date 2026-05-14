package com.dp.advancedgunnerycontrol.persistence

import com.dp.advancedgunnerycontrol.settings.Settings

typealias StorageBaseIntKey<ValueType> = StorageBase<Int, ValueType>
open class StorageBase<KeyType, ValueType>(val persistentDataKey: String) {

    companion object {
        fun <KeyType, ValueType> assembleStorageArray(baseKey: String, size: Int = Settings.maxLoadouts()): List<StorageBase<KeyType, ValueType>> {
            val storageSlots = mutableListOf(StorageBase<KeyType, ValueType>(baseKey))
            for (slotIndex in 1 until size) {
                storageSlots.add(StorageBase(baseKey + slotIndex.toString()))
            }
            return storageSlots.toList()
        }
    }

    private fun getMap(wasFallback: Boolean = false): MutableMap<String, MutableMap<KeyType, ValueType>> {
        val persistentData = agcPersistentDataOrNull("storage '$persistentDataKey'") ?: return mutableMapOf()
        return (persistentData[persistentDataKey] as? MutableMap<String, MutableMap<KeyType, ValueType>>?)
            ?: kotlin.run {
                persistentData.remove(persistentDataKey)
                persistentData[persistentDataKey] =
                    mutableMapOf<String, MutableMap<KeyType, ValueType>>()
                if (wasFallback) return mutableMapOf()
                return getMap(true)
            }
    }

    var modesByShip: MutableMap<String, MutableMap<KeyType, ValueType>>
        get() {
            if (!Settings.enablePersistentModes()) {
                return mutableMapOf()
            }
            return getMap()
        }
        set(value) {
            agcPersistentDataOrNull("storage '$persistentDataKey' write")?.set(persistentDataKey, value)
        }

    inline fun <reified T> purgeIfNecessary(
        crossinline keyIsValid: (Any?) -> Boolean = { true },
        crossinline valueIsValid: (Any?) -> Boolean = { it is T },
    ) {
        val persistentData = agcPersistentDataOrNull("storage '$persistentDataKey' purge validation") ?: return
        val map = persistentData[persistentDataKey] as? MutableMap<*, *>?
        if (map == null) {
            purge(); return
        }
        if (map.isEmpty()) {
            purge(); return
        }
        val validShape = map.all { (shipId, loadoutValues) ->
            shipId is String &&
                loadoutValues is MutableMap<*, *> &&
                loadoutValues.all { (key, value) ->
                    keyIsValid(key) && valueIsValid(value)
                }
        }
        if (!validShape) {
            purge(); return
        }
        if (!Settings.enablePersistentModes()) {
            purge(); return
        }
    }

    fun purge() {
        agcPersistentDataOrNull("storage '$persistentDataKey' purge")?.remove(persistentDataKey)
    }
}
