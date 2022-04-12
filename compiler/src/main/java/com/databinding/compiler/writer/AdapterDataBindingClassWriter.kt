package com.databinding.compiler.writer

import com.databinding.compiler.AbstractClassWriter
import com.databinding.compiler.Utils
import com.databinding.compiler.Utils.codeString
import com.databinding.compiler.Utils.combineClassName
import com.databinding.compiler.Utils.simpleName
import com.databinding.compiler.items.BindableObjectImpl
import com.databinding.compiler.items.BindingMethodImpl
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.StandardLocation

class AdapterDataBindingClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(
    filer, messager
) {
    @Throws(IOException::class)
    fun writeAdapterDataBindingClass(
        bindableObjects: MutableMap<String, BindableObjectImpl>,
        methods: MutableMap<String, BindingMethodImpl>
    ) {
        val packageName: String
        val lastDot = "com.databinding.databinding.AdapterDataBinding".lastIndexOf('.')
        packageName = "com.databinding.databinding.AdapterDataBinding".substring(0, lastDot)
        val filerSourceFile = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            packageName, "AdapterDataBinding" + ".kt"
        )
        PrintWriter(filerSourceFile.openWriter()).use { out ->
            out.print("package ")
            out.print(packageName)
            out.println("")
            out.println()
            out.println("import android.content.Context")
            out.println("import com.databinding.databinding.IAdapterDataBinding")
            out.println("import com.databinding.databinding.DataBinding")
            val imports: MutableSet<String> = HashSet()
            for (bindableData in bindableObjects.values) {
                writeImport(bindableData.viewClassName, imports, out)
                writeImport(bindableData.className, imports, out)
            }
            out.println()
            out.print("class AdapterDataBinding: IAdapterDataBinding { \n\n")

            // instance variable
            out.print("  val dataBinding: DataBinding \n\n")

            // constructor
            out.print("  constructor(dataBinding: DataBinding) { \n")
            out.print("    this.dataBinding = dataBinding \n")
            out.print("  } \n\n")
            out.print("  override fun bind(view: IView, data: IData) { \n")
            out.print("    when(pair(view, data)) { \n")
            for (`object` in bindableObjects.values) {
                val simpleViewName = simpleName(`object`.viewClassName)
                val simpleDataName = simpleName(`object`.className)
                val key = combineClassName(simpleViewName, simpleDataName)
                if (methods.containsKey(key)) {
                    out.print(
                        "     ${codeString(simpleViewName + simpleDataName)} -> dataBinding.bind(view as $simpleViewName,  data as $simpleDataName) \n"
                    )
                }
            }
            out.print("      else -> throw RuntimeException(\"Cannot bind view for \" + pair(view, data)) \n")
            out.print("    } \n")
            out.print("  } \n\n")

            // helper method
            out.print("  fun pair(view: IView, data: IData) : String { \n")
            out.print("    return view.name() + data.name() \n")
            out.print("  } \n\n")
            out.println("}")
        }
    }

    private fun writeImport(import: String, imports: MutableSet<String>, out: PrintWriter) {
        if (import !in imports && Utils.canImport(import)) {
            out.println("import $import")
            imports.add(import)
        }
    }
}