package com.databinding.compiler

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

abstract class AbstractClassWriter(protected var filer: Filer, protected var messager: Messager) {
    /**
     * Error method
     */
    private fun error(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }
}