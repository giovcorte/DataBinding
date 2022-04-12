package com.databinding.compiler

import com.databinding.annotations.*
import com.databinding.compiler.Utils.cleanPath
import com.databinding.compiler.items.*
import com.databinding.compiler.writer.AdapterDataBindingClassWriter
import com.databinding.compiler.writer.DataBindingClassWriter
import com.databinding.compiler.writer.IViewFactoryClassWriter
import java.io.IOException
import java.util.regex.Pattern
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

class DataBindingCompiler: AbstractProcessor() {

    lateinit var filer: Filer
    lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        if (set.isEmpty()) {
            return false
        }

        val methods: MutableMap<String, BindingMethodImpl> = LinkedHashMap() // <"TextView:TextModel" - BindingMethod obj>
        val views: MutableMap<String, BindableViewImpl> = LinkedHashMap() // <"android.view.TextView" - BindableView obj>
        val objects: MutableMap<String, BindableObjectImpl> = LinkedHashMap() // <"com.myapp.MyModel" - BindableObject obj>

        // @BindingMethod annotated methods
        for (element in roundEnvironment.getElementsAnnotatedWith(BindingMethod::class.java)) {
            val method = element as ExecutableElement
            if (!method.modifiers.contains(Modifier.PUBLIC) || !method.modifiers.contains(Modifier.STATIC)) {
                error(method.simpleName.toString() + " must be public and static")
                continue
            }
            val methodName = method.simpleName.toString()
            val enclosingClass = element.getEnclosingElement().asType().toString()
            var viewClass: String? = null
            var dataClass: String? = null
            val dependencies: MutableList<String> = ArrayList()
            val parameters = method.parameters
            var isValidMethod = true
            for ((currentParameterIndex, param) in parameters.withIndex()) {
                val viewAnnotated = param.getAnnotation(View::class.java)
                val dataAnnotated = param.getAnnotation(Data::class.java)
                val injectAnnotated = param.getAnnotation(Inject::class.java)

                if (viewAnnotated != null) {
                    if (viewClass == null && currentParameterIndex == 0) {
                        viewClass = param.asType().toString()
                    } else {
                        isValidMethod = false
                        error("@BindingMethod $methodName -> only first parameter can be annotated with @View")
                    }
                } else if (dataAnnotated != null) {
                    if (dataClass == null && currentParameterIndex == 1) {
                        dataClass = param.asType().toString()
                    } else {
                        isValidMethod = false
                        error("@BindingMethod $methodName -> only first parameter can be annotated with @View")
                    }
                } else if (injectAnnotated != null && currentParameterIndex >= 2) {
                    dependencies.add(param.asType().toString())
                } else {
                    isValidMethod = false
                    error("@BindingMethod $methodName -> all parameter must be annotated")
                }
            }
            if (viewClass == null || dataClass == null) {
                error("@BindingMethod $methodName -> must have a parameter @View annotated and one @Data annotated")
                isValidMethod = false
            }
            if (isValidMethod) {
                val bindingMethod = BindingMethodImpl(
                    enclosingClass,
                    methodName,
                    viewClass!!,
                    dataClass!!,
                    dependencies
                )
                methods[Utils.combineClassName(
                    Utils.simpleName(viewClass),
                    Utils.simpleName(dataClass)
                )] =
                    bindingMethod
            }
        }

        // @BindableView annotated android custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(BindableView::class.java)) {
            val className = element.asType().toString()
            val interfaces = (element as TypeElement).interfaces
            val implementIView = isImplementingInterface(interfaces)
            if (!views.containsKey(className)) {
                views[className] = BindableViewImpl(className, implementIView)
            }
        }

        // @BindTo annotated fields of @BindableView annotated android custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(BindWith::class.java)) {
            val item = element.getAnnotation(BindWith::class.java)
            val enclosingClassName = element.enclosingElement.asType().toString()
            val viewClassName = element.asType().toString()
            val fieldName = element.simpleName.toString()
            val paths = item.paths
            if (duplicatePathExist(paths, DATA_PATH_REGEX)) {
                error("$enclosingClassName $fieldName -> cannot exist a duplicate path for the same data model")
                continue
            }
            if (!views.containsKey(enclosingClassName)) {
                views[enclosingClassName] = BindableViewImpl(enclosingClassName)
            }
            val enclosingView = views[enclosingClassName]
            for (path in paths) {
                if (!isValidPath(path, DATA_PATH_REGEX)) {
                    error(enclosingView!!.className + " view -> " + path + " is not a valid path")
                    continue
                }
                val simpleDataClassForField = Utils.getDataClassFromPath(path)
                val fieldViewSimpleClassName = Utils.simpleName(viewClassName)
                val fieldObjectSimpleClassName = Utils.getTargetDataClassFromPath(path)
                val viewDataPairForBindingField =
                    Utils.combineClassName(fieldViewSimpleClassName, fieldObjectSimpleClassName)
                if (!enclosingView!!.bindableViewFields.containsKey(simpleDataClassForField)) {
                    enclosingView.bindableViewFields[simpleDataClassForField] = ArrayList()
                }
                val fields = enclosingView.bindableViewFields[simpleDataClassForField]
                if (methods.containsKey(viewDataPairForBindingField)) {
                    fields!!.add(
                        BindableViewFieldImpl(
                            fieldName,
                            cleanPath(path),
                            viewClassName,
                            fieldObjectSimpleClassName
                        )
                    )
                } else {
                    error("The view:data $viewDataPairForBindingField pair -> has not a binding method")
                }
            }
        }

        // @BindAction annotated fields on custom views
        for (element in roundEnvironment.getElementsAnnotatedWith(BindAction::class.java)) {
            val item = element.getAnnotation(BindAction::class.java)
            if (element.kind == ElementKind.FIELD) {
                val enclosingClassName = element.enclosingElement.asType().toString()
                val viewClassName = element.asType().toString()
                val fieldName = element.simpleName.toString()
                val paths = item.paths
                if (duplicatePathExist(paths, ACTION_PATH_REGEX)) {
                    error("$enclosingClassName $fieldName -> cannot exist a duplicate path for the same data model")
                    continue
                }
                if (!views.containsKey(enclosingClassName)) {
                    views[enclosingClassName] = BindableViewImpl(enclosingClassName)
                }
                val enclosingView = views[enclosingClassName]
                for (path in paths) {
                    if (!isValidPath(path, ACTION_PATH_REGEX)) {
                        error(enclosingView!!.className + " view -> " + path + " is not a valid path")
                        continue
                    }
                    val simpleDataClassForField = Utils.getDataClassFromPath(path)
                    val cleanPath = cleanPath(path)
                    if (!enclosingView!!.bindableActionFields.containsKey(simpleDataClassForField)) {
                        enclosingView.bindableActionFields[simpleDataClassForField] = ArrayList()
                    }
                    val fields = enclosingView.bindableActionFields[simpleDataClassForField]
                    fields!!.add(BindableActionFieldImpl(fieldName, cleanPath))
                }
            } else if (element.kind == ElementKind.CLASS) {
                val className = element.asType().toString()
                for (path in item.paths) {
                    val objectSimpleClassName = Utils.getDataClassFromPath(path)
                    val cleanPath = cleanPath(path)
                    views[className]!!.actions[objectSimpleClassName] =
                        BindableActionImpl(className, objectSimpleClassName, cleanPath)
                }
            }
        }

        // @BindableObject annotated data models
        for (element in roundEnvironment.getElementsAnnotatedWith(BindableObject::class.java)) {
            val item = element.getAnnotation(BindableObject::class.java)
            val viewClassName = getClassFromAnnotation(item)
            val objectClassName = element.asType().toString()
            objects[objectClassName] = BindableObjectImpl(objectClassName, viewClassName)
        }

        try {
            val dataBindingClassWriter = DataBindingClassWriter(filer, messager)
            dataBindingClassWriter.writeDataBindingClass(views, methods)

            val adapterDataBindingClassWriter = AdapterDataBindingClassWriter(filer, messager)
            adapterDataBindingClassWriter.writeAdapterDataBindingClass(objects, methods)

            val viewFactoryClassWriter = IViewFactoryClassWriter(filer, messager)
            viewFactoryClassWriter.writeIViewFactoryClass(objects, views)
        } catch (e: IOException) {
            error(e.message!!)
        }

        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations: MutableSet<String> = LinkedHashSet()
        annotations.add(BindAction::class.java.canonicalName)
        annotations.add(View::class.java.canonicalName)
        annotations.add(BindWith::class.java.canonicalName)
        annotations.add(BindableView::class.java.canonicalName)
        annotations.add(BindableObject::class.java.canonicalName)
        annotations.add(Data::class.java.canonicalName)
        annotations.add(Inject::class.java.canonicalName)
        return annotations
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    /**
     * Utility methods
     */
    private fun duplicatePathExist(paths: Array<String>, regex: String): Boolean {
        var duplicates = false
        for (i in paths.indices) {
            for (k in i + 1 until paths.size) {
                val iPath = paths[i]
                val kPath = paths[k]
                if (isValidPath(iPath, regex) && isValidPath(kPath, regex)) {
                    if (Utils.getDataClassFromPath(iPath) == Utils.getDataClassFromPath(kPath)) {
                        duplicates = true
                    }
                }
            }
        }
        return duplicates
    }

    private fun isValidPath(path: String, regex: String): Boolean {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(path)
        return matcher.matches()
    }

    private fun isImplementingInterface(interfaces: List<TypeMirror>): Boolean {
        var found = false
        for (current in interfaces) {
            if (current.toString() == I_VIEW_INTERFACE_CLASS) {
                found = true
                break
            }
        }
        return found
    }

    private fun getClassFromAnnotation(annotation: BindableObject): String {
        return try {
            val value: KClass<*> = annotation.view
            value.qualifiedName!!
        } catch (mte: MirroredTypeException) {
            processingEnv.typeUtils.asElement(mte.typeMirror).asType().toString()
        }
    }

    /**
     * Error methods
     */
    private fun error(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    companion object {
        private const val DATA_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+:)([A-Z][\\w]+)"
        private const val ACTION_PATH_REGEX = "^([A-Z][\\w]+\\.)([\\w]+\\.)?+([\\w]+)"
        private const val I_VIEW_INTERFACE_CLASS = "com.databinding.databinding.IView"
    }

}