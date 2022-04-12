package com.databinding.compiler

import java.lang.StringBuilder
import java.util.*

/**
 * Utility methods
 */
object Utils {

    private val NO_IMPORT = arrayOf("java.lang.String", "java.lang.Boolean", "java.lang.Integer")

    fun canImport(import: String): Boolean {
        return import !in NO_IMPORT
    }

    fun capitalize(name: String): String {
        return if (name.length == 1) {
            name.uppercase(Locale.getDefault())
        } else name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
    }

    fun lower(s: String): String {
        return Character.toLowerCase(s[0]).toString() + s.substring(1)
    }

    fun codeString(s: String?): String {
        return if (s == null) {
            "null"
        } else "\"" + s + "\""
    }

    fun typedParams(constructorParameters: List<String>): String {
        val params = StringBuilder()
        for (i in constructorParameters.indices) {
            val param = constructorParameters[i]
            params.append(lower(simpleName(param)))
            params.append(": ")
            params.append(simpleName(param))
            if (i <= constructorParameters.size - 2) {
                params.append(", ")
            }
        }
        return params.toString()
    }

    fun params(parameters: List<String>): String {
        val params = StringBuilder()
        for (i in parameters.indices) {
            val dependency = parameters[i]
            params.append(lower(simpleName(dependency)))
            if (i < parameters.size - 1) {
                params.append(", ")
            }
        }
        return params.toString()
    }

    fun combineClassName(simpleViewClass: String, simpleDataCLass: String): String {
        return "$simpleViewClass:$simpleDataCLass"
    }

    fun cleanPath(path: String): String {
        var result = path
        if (result.contains(".")) {
            result = result.substring(result.indexOf(".") + 1)
        }
        if (result.contains(":")) {
            result = result.substring(0, result.indexOf(":"))
        }
        return result
    }

    fun getDataClassFromPath(path: String): String {
        return if (path.contains(".")) {
            path.substring(0, path.indexOf("."))
        } else path
    }

    fun getTargetDataClassFromPath(path: String): String {
        return if (path.contains(":")) {
            path.substring(path.indexOf(":") + 1)
        } else path
    }

    fun simpleName(className: String): String {
        return if (className.contains(".")) {
            className.substring(className.lastIndexOf(".") + 1)
        } else className
    }

    fun dataPath(path: String): String {
        val pathSegments = path.split(".")
        var result = ""
        for (i in pathSegments.indices) {
            result += pathSegments[i]
            if (i < pathSegments.size - 1) {
                result += "?."
            }
        }

        return result
    }
}