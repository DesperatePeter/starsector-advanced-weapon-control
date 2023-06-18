package com.dp.advancedgunnerycontrol.utils

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

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
        return emptyList()
    }
}

fun getMethodNames(instanceToGetFrom: Any): List<String?>{
    val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))

    val instancesOfFields = instanceToGetFrom.javaClass.declaredMethods
    return instancesOfFields.map { getNameMethod.invoke(it) as? String }
}

fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?, declared: Boolean = false) : Any?
{
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
}

/**
 * if multiple methods return this type, one will be evoked arbitrarily
 */
fun invokeMethodThatReturnsType(instance: Any, returnTypeNameContains: String, vararg arguments: Any?): Any?{
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
}