package com.databinding.databinding

import android.util.Patterns
import java.io.File

/**
 * Utilities class.
 */
object Utils {
    /**
     * Returns true if str is a integer, false otherwise. It's optimized for performance.
     *
     * @param str String to test.
     * @return boolean true if str is integer, false otherwise.
     */
    fun isInteger(str: String?): Boolean {
        if (str == null) {
            return false
        }
        val length = str.length
        if (length == 0) {
            return false
        }
        var i = 0
        if (str[0] == '-') {
            if (length == 1) {
                return false
            }
            i = 1
        }
        while (i < length) {
            val c = str[i]
            if (c < '0' || c > '9') {
                return false
            }
            i++
        }
        return true
    }

    /**
     * Determines if a string conforms to a url pattern.
     *
     * @param source String maybe url.
     * @return boolean true if String represents a url, false otherwise.
     */
    fun isUrl(source: String): Boolean {
        return Patterns.WEB_URL.matcher(source).matches()
    }

    /**
     * Determines if the source is a valid File path.
     *
     * @param source String maybe file path.
     * @return boolean true if String represents a valid file, false otherwise.
     */
    fun isFile(source: String): Boolean {
        val file = File(source)
        return file.exists()
    }

    /**
     * Determines if the source can be a drawable resource.
     *
     * @param source String meybe resource
     * @return True if the source is numeral, false otherwise.
     */
    fun isResource(source: String): Boolean {
        return isInteger(source)
    }
}