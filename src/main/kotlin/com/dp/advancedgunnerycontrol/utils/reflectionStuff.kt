package com.dp.advancedgunnerycontrol.utils

import com.fs.starfarer.api.Global
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


class AdvancedGunneryControlLogClass

fun getFieldsByName(fieldName: String, instanceToGetFrom: Any): List<Any?> {
    try {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
        val setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        val instancesOfFields: Array<out Any> = instanceToGetFrom.javaClass.declaredFields
        val toReturn = mutableListOf<Any?>()
        for (obj in instancesOfFields)
        {
            setAccessMethod.invoke(obj, true)
            val name = getNameMethod.invoke(obj)
            if (name.toString() == fieldName)
            {
                val x = getMethod.invoke(obj, instanceToGetFrom)
                toReturn.add(x)
            }
        }
        return toReturn
    }catch (e: Exception){
        return emptyList()
    }
}

fun getFieldsByTypeName(typeNameContains: String, instanceToGetFrom: Any): List<Any?> {
    try {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getTypeMethod = MethodHandles.lookup().findVirtual(fieldClass, "getType", MethodType.methodType(Class::class.java))
        val setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        val instancesOfFields: Array<out Any> = instanceToGetFrom.javaClass.declaredFields
        val toReturn = mutableListOf<Any?>()
        for (obj in instancesOfFields)
        {
            setAccessMethod.invoke(obj, true)
            val type = getTypeMethod.invoke(obj)
            if (type.toString().contains(typeNameContains))
            {
                val x = getMethod.invoke(obj, instanceToGetFrom)
                toReturn.add(x)
            }
        }
        return toReturn
    }catch (e: Exception){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error("Failed invoking method, returning null")
        return emptyList()
    }
}

fun getMethodNames(instanceToGetFrom: Any): List<String?>{
    return try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))

        val instancesOfFields = instanceToGetFrom.javaClass.declaredMethods
        instancesOfFields.map { getNameMethod.invoke(it) as? String }
    }catch (e: Throwable){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error("Failed invoking method, returning null")
        listOf()
    }
}

fun hasMethodNamed(instanceToGetFrom: Any, methodName: String): Boolean{
    return getMethodNames(instanceToGetFrom).any {
        it == methodName
    }
}

fun invokeMethodByName(methodName: String, instance: Any, vararg arguments: Any?, declared: Boolean = false) : Any?
{
    try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null

        if (!declared)
        {
            for (method in instance::class.java.methods as Array<Any>)
            {
                if (getNameMethod.invoke(method) == methodName)
                {
                    foundMethod = method
                }
            }
        }
        else
        {
            for (method in instance::class.java.declaredMethods as Array<Any>)
            {
                if (getNameMethod.invoke(method) == methodName)
                {
                    foundMethod = method
                }
            }
        }

        return invokeMethod.invoke(foundMethod, instance, arguments)
    }catch (e: Throwable){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error("Failed invoking method, returning null")
        return null
    }
}

/**
 * if multiple methods return this type, one will be evoked arbitrarily
 */
fun invokeMethodThatReturnsType(instance: Any, returnTypeNameContains: String, vararg arguments: Any?): Any?{
    try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getReturnTypeMethod = MethodHandles.lookup().findVirtual(methodClass, "getReturnType", MethodType.methodType(Class::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null

        for (method in instance::class.java.declaredMethods as Array<Any>)
        {
            if (getReturnTypeMethod.invoke(method).toString().contains(returnTypeNameContains))
            {
                foundMethod = method
            }
        }
        return invokeMethod.invoke(foundMethod, instance, arguments)
    }catch (e: Throwable){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error("Failed invoking method, returning null")
        return null
    }
}
