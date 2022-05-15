package com.databinding.compiler.writer

import com.databinding.compiler.AbstractClassWriter
import com.databinding.compiler.Utils.canImport
import com.databinding.compiler.Utils.combineClassName
import com.databinding.compiler.Utils.dataPath
import com.databinding.compiler.Utils.typedParams
import com.databinding.compiler.Utils.params
import com.databinding.compiler.Utils.lower
import com.databinding.compiler.Utils.simpleName
import com.databinding.compiler.items.*
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.StandardLocation

class DataBindingClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(filer, messager) {

    @Throws(IOException::class)
    fun writeDataBindingClass(views: Map<String, BindableViewImpl>, methods: Map<String, BindingMethodImpl>) {
        val packageName: String
        val lastDot = "com.databinding.databinding.DataBinding".lastIndexOf('.')
        packageName = "com.databinding.databinding.DataBinding".substring(0, lastDot)
        val simpleClassName = "com.databinding.databinding.DataBinding".substring(lastDot + 1)

        val filerSourceFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "DataBinding" + ".kt")

        PrintWriter(filerSourceFile.openWriter()).use { out ->
            // basic imports
            out.print("package $packageName")
            out.println("import com.databinding.databinding.DataBindingHelper")
            out.println("import android.view.View")

            // user imports
            val imports: MutableSet<String> = HashSet()
            val dependenciesImports: MutableSet<String> = HashSet()

            for (method in methods.values) {
                writeImport(method.enclosingClassName, imports, out)
                writeImport(method.dataClassName, imports, out)
                writeImport(method.viewClassName, imports, out)

                for (dependency in method.dependencies) {
                    writeImport(dependency, imports, out)
                    dependenciesImports.add(dependency)
                }
            }
            for (view in views.values) {
                writeImport(view.className, imports, out)

                for (fields in view.bindableViewFields.values) {
                    for (field in fields) {
                        writeImport(field.fieldViewClassName, imports, out)
                    }
                }
            }
            out.println()

            // open class
            out.print("class $simpleClassName { \n\n")

            // instance variables
            for (dependency in dependenciesImports) {
                out.print("  val ${lower(simpleName(dependency))}: ${simpleName(dependency)} \n")
            }
            out.print("\n  public companion object { public lateinit var instance: DataBinding } \n\n")

            // constructor
            out.print("  constructor(${typedParams(ArrayList(dependenciesImports))}) { \n")
            for (dependency in dependenciesImports) {
                out.print("    this.${lower(simpleName(dependency))} = ${lower(simpleName(dependency))} \n")
            }
            out.print("    instance = this \n")
            out.print("  } \n\n")

            // bind overloaded methods
            for (viewModelPair in methods.keys) {
                val method = methods[viewModelPair]!!

                val simpleViewClassName = simpleName(method.viewClassName)
                val simpleModelClassName = simpleName(method.dataClassName)
                val enclosingClassName = method.enclosingClassName
                val methodName = method.methodName

                // binding method start
                out.print("  public fun bind(view: $simpleViewClassName?, data: $simpleModelClassName?) { \n")
                if (method.dependencies.isNotEmpty()) { // method with dependencies
                    out.print("    ${simpleName(enclosingClassName)}.$methodName(view, data, ${params(method.dependencies)}) \n")
                } else { // method with only view and data
                    out.print("    ${simpleName(enclosingClassName)}.$methodName(view, data) \n")
                }

                // this is a custom view
                if (views.containsKey(method.viewClassName)) {
                    // main view action for this data class
                    val bindableView = views[method.viewClassName]!!
                    val actionsMap: Map<String, BindableActionImpl> = bindableView.actions

                    if (actionsMap.containsKey(simpleModelClassName)) {
                        val action = actionsMap[simpleModelClassName]
                        out.print("    DataBindingHelper.bindAction(view, data?.${action!!.path}) \n")
                    }

                    // view field binding
                    val viewFields: Map<String, List<BindableViewFieldImpl>> = bindableView.bindableViewFields

                    if (viewFields.containsKey(simpleModelClassName)) {
                        val fields = viewFields[simpleModelClassName]!!

                        for (field in fields) {
                            val simpleFieldViewClass = simpleName(field.fieldViewClassName)
                            val simpleFieldDataClass = field.fieldObjectClassName
                            val key = combineClassName(simpleFieldViewClass, simpleFieldDataClass)

                            if (methods.containsKey(key)) {
                                out.print("    bind(view?.${field.fieldName} , data?.${dataPath(field.objectPath)}) \n")
                            }
                        }
                    }

                    // view field actions
                    val actionFields: Map<String, List<BindableActionFieldImpl>> = bindableView.bindableActionFields

                    if (actionFields.containsKey(simpleModelClassName)) {
                        val fields = actionFields[simpleModelClassName]!!

                        for (field in fields) {
                            out.print(
                                "     DataBindingHelper.bindAction(view?.${field.fieldName} , data?.${dataPath(field.objectPath)}) \n"
                            )
                        }
                    }
                }
                out.print("  } \n\n")
            }

            // close class
            out.print("}")
        }
    }

    private fun writeImport(import: String, imports: MutableSet<String>, out: PrintWriter) {
        if (import !in imports && canImport(import)) {
            out.println("import $import")
            imports.add(import)
        }
    }
}