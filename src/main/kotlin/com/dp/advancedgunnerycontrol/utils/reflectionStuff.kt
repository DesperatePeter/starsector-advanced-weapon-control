package com.dp.advancedgunnerycontrol.utils

import com.fs.starfarer.api.Global
import java.lang.RuntimeException
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


class AdvancedGunneryControlLogClass

// will randomly throw exceptions when set to true
const val TEST_MODE = false

fun getFieldsByName(fieldName: String, instanceToGetFrom: Any, narrativeContext: String? = null): List<Any?> {
    try {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
        val setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        throwOccasionally()

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
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error(
            "Tried to get fields named $fieldName of object of type ${instanceToGetFrom.javaClass}." +
                    " Failed due to the following exception: $e"
        )
        narrativeContext?.let {
            Global.getLogger(AdvancedGunneryControlLogClass().javaClass).warn(
                "Provided context: $it"
            )
        }
        return emptyList()
    }
}

fun getFieldsByTypeName(typeNameContains: String, instanceToGetFrom: Any, narrativeContext: String? = null): List<Any?> {
    try {
        val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
        val getMethod = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
        val getTypeMethod = MethodHandles.lookup().findVirtual(fieldClass, "getType", MethodType.methodType(Class::class.java))
        val setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass,"setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

        throwOccasionally()

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
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error(
            "Tried to get fields of types that contain $typeNameContains of object of type ${instanceToGetFrom.javaClass}." +
                    " Failed due to the following exception: $e")
        narrativeContext?.let {
            Global.getLogger(AdvancedGunneryControlLogClass().javaClass).warn(
                "Provided context: $it"
            )
        }
        return emptyList()
    }
}

fun getMethodNames(instanceToGetFrom: Any, narrativeContext: String? = null): List<String?>{
    return try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))

        throwOccasionally()

        val instancesOfFields: Array<out Any> = instanceToGetFrom.javaClass.getDeclaredMethods()
        instancesOfFields.map { getNameMethod.invoke(it) as? String }
    }catch (e: Throwable){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error(
            "Tried to get method names of object of type ${instanceToGetFrom.javaClass}." +
                    " Failed due to the following exception: $e")
        narrativeContext?.let {
            Global.getLogger(AdvancedGunneryControlLogClass().javaClass).warn(
                "Provided context: $it"
            )
        }
        listOf()
    }
}

fun hasMethodNamed(instanceToGetFrom: Any, methodName: String, narrativeContext: String? = null): Boolean{
    return getMethodNames(instanceToGetFrom, narrativeContext).any {
        it == methodName
    }
}

fun invokeMethodByName(methodName: String, instance: Any, vararg arguments: Any?, declared: Boolean = false, narrativeContext: String? = null) : Any?
{
    try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getNameMethod = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        throwOccasionally()

        var exception: Throwable? = null

        if (!declared)
        {
            for (method in instance::class.java.methods as Array<Any>)
            {
                if (getNameMethod.invoke(method) == methodName)
                {
                    try {
                        return invokeMethod.invoke(method, instance, arguments)
                    }catch (e: Throwable) {
                        exception = e
                        continue
                    }
                }
            }
        }
        else
        {
            for (method in instance::class.java.declaredMethods as Array<Any>)
            {
                if (getNameMethod.invoke(method) == methodName)
                {
                    try {
                        return invokeMethod.invoke(method, instance, arguments)
                    }catch (e: Throwable) {
                        exception = e
                        continue
                    }
                }
            }
        }

        exception?.let { throw it }

        throw NoSuchMethodError("The method to invoke could not be found")


    }catch (e: Throwable){
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error(
            "Tried to invoke method named $methodName of object of type ${instance.javaClass} with arguments: $arguments." +
                    " Failed due to the following exception: $e")
        narrativeContext?.let {
            Global.getLogger(AdvancedGunneryControlLogClass().javaClass).warn(
                "Provided context: $it"
            )
        }
        return null
    }
}

/**
 * if multiple methods return this type, one will be evoked arbitrarily
 */
fun invokeMethodThatReturnsType(instance: Any, returnTypeNameContains: String, vararg arguments: Any?, narrativeContext: String? = null): Any?{
    try {
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val getReturnTypeMethod = MethodHandles.lookup().findVirtual(methodClass, "getReturnType", MethodType.methodType(Class::class.java))
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        throwOccasionally()

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
        Global.getLogger(AdvancedGunneryControlLogClass().javaClass).error(
            "Tried to invoke a method that returns a type containing $returnTypeNameContains of object of type ${instance.javaClass} with arguments: $arguments." +
                    " Failed due to the following exception: $e")
        narrativeContext?.let {
            Global.getLogger(AdvancedGunneryControlLogClass().javaClass).warn(
                "Provided context: $it"
            )
        }
        return null
    }
}

fun throwOccasionally(){
    if(!TEST_MODE) return
    if(Math.random() < 0.05f) throw RuntimeException("This is a randomly thrown exception used for testing. This shouldn't happen in a release build.")
}
