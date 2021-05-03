package com.dp.advancedgunnerycontrol.settings
import com.fs.starfarer.api.Global
import org.json.JSONException
import org.json.JSONObject
// Note: I can't seem to find any info on template/generics-specialization or SFINAE in Kotlin,
// so instead I implemented different types of property.
// This whole thing feels unsatisfying...
open class Setting<T>(private val key: String, defaultValue: T)
{
    var value :T = defaultValue
    fun load(json : JSONObject){
        try{
            value = (loadImpl(json) ?: kotlin.run {
                logError()
                value
            })
        }catch (e: JSONException){
            logError()
        }
    }
    open fun loadImpl(json: JSONObject) : T?{
        return json.get(key) as? T
    }
    operator fun invoke() : T {
        return value
    }

    fun set(value: T){
        this.value = value
    }

    private fun logError(){
        Global.getLogger(this.javaClass).warn("""
                Error when loading $key, using default default value $value instead.
            """.trimIndent())
    }
}

class CharSetting (private val key: String, defaultValue: Char): Setting<Char>(key, defaultValue)
{
    override fun loadImpl(json : JSONObject) : Char?{
        return ((json.get(key) as? String)?.get(0)?.toLowerCase())
    }
}

class BoolSetting (private val key: String, defaultValue: Boolean): Setting<Boolean>(key, defaultValue)
{
    override fun loadImpl(json : JSONObject) : Boolean{
        return json.get(key) == true
    }
}

class ListSetting (private val key: String, defaultValue: List<String>): Setting<List<String>>(key, defaultValue)
{
    override fun loadImpl(json : JSONObject) : List<String>{
        val list = mutableListOf<String>()
        json.apply {
            val array = getJSONArray(key)
            for (i in 0 until array.length()){
                list.add(array.getString(i))
            }
        }
        return list
    }
}
