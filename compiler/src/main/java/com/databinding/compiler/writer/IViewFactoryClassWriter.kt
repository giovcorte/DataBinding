package com.databinding.compiler.writer

import com.databinding.compiler.AbstractClassWriter
import com.databinding.compiler.Utils.codeString
import com.databinding.compiler.Utils.simpleName
import com.databinding.compiler.items.BindableObjectImpl
import com.databinding.compiler.items.BindableViewImpl
import java.io.IOException
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.StandardLocation

class IViewFactoryClassWriter(filer: Filer, messager: Messager) : AbstractClassWriter(
    filer, messager
) {
    @Throws(IOException::class)
    fun writeIViewFactoryClass(
        objects: Map<String, BindableObjectImpl>,
        views: Map<String, BindableViewImpl>
    ) {
        val packageName: String
        val lastDot = "com.databinding.databinding.factory.ViewFactory".lastIndexOf('.')
        packageName = "com.databinding.databinding.factory.ViewFactory".substring(0, lastDot)

        val filerSourceFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "ViewFactory" + ".kt")

        PrintWriter(filerSourceFile.openWriter()).use { out ->
            out.print("package ")
            out.print(packageName)
            out.println()
            out.println("import android.view.View")
            out.println("import android.content.Context")
            out.println("import com.databinding.databinding.IViewFactory")
            out.println("import com.databinding.databinding.IView")
            out.println("import com.databinding.databinding.IData")

            val imports: MutableSet<String> = HashSet()
            for (data in objects.values) {
                if (data.viewClassName !in imports) {
                    out.println("import ${data.viewClassName}")
                    imports.add(data.viewClassName)
                }
            }
            out.println()
            out.print("class ViewFactory: IViewFactory { \n\n")

            // instance variable
            out.print("  val context: Context \n\n")

            // constructor
            out.print("  constructor(context: Context) { \n")
            out.print("    this.context = context \n")
            out.print("  } \n\n")
            out.print("  override fun build(data: IData): IView { \n")
            out.print("    when(data.name()) { \n")
            for (data in objects.values) {
                if (views.containsKey(data.viewClassName) && views[data.viewClassName]!!.implementIView) {
                    out.print("      ${codeString(simpleName(data.className))} -> return ${simpleName(data.viewClassName)}(context) \n")
                }
            }
            out.print("      else -> throw RuntimeException(\"Cannot create view for \" + data.name()) \n")
            out.print("    } \n")
            out.print("  } \n\n")
            out.println("}")
        }
    }
}