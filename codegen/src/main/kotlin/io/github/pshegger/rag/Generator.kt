package io.github.pshegger.rag

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.UNIT
import io.github.pshegger.rag.builders.AdapterBuilder
import io.github.pshegger.rag.utils.AdapterInfo
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class Generator : AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(ModelBinding::class.java.name)

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false

        roundEnv.getElementsAnnotatedWith(ModelBinding::class.java)
            .mapNotNull { element ->
                if (element.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes can be annotated with @ModelBinding")
                    return@mapNotNull null
                }
                generateAdapterInfo(element)
            }
            .map { adapterInfo ->
                val fileName = "${adapterInfo.namePrefix}Adapter"
                val fileBuilder = FileSpec.builder(adapterInfo.pack, fileName)
                fileBuilder.addType(AdapterBuilder(processingEnv, adapterInfo).build()).build()
            }
            .forEach { fileSpec ->
                fileSpec.writeTo(File(kaptKotlinGeneratedDir))
            }

        return true
    }

    private fun generateAdapterInfo(bindingClassElement: Element): AdapterInfo? {
        val pack = processingEnv.elementUtils.getPackageOf(bindingClassElement).toString()
        val constructor = bindingClassElement.enclosedElements.find { element ->
            element.kind == ElementKind.CONSTRUCTOR && (element as ExecutableElement).parameters.size == 1
        } as? ExecutableElement

        if (constructor == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "Skipping ${bindingClassElement.asType()}: cannot find valid constructor"
            )
            return null
        }

        val annotation = bindingClassElement.getAnnotation(ModelBinding::class.java)
        val item = constructor.parameters[0]
        val itemPackage = processingEnv.elementUtils.getPackageOf(item).toString()
        val itemClassName = item.asType().toString().drop(itemPackage.length + 1)
        val itemClickListenerType = if (annotation.clickListener) {
            LambdaTypeName.get(null, INT, returnType = UNIT)
        } else null
        val namePrefix = if (annotation.namePrefix.isNotBlank()) {
            annotation.namePrefix.trim()
        } else itemClassName

        return AdapterInfo(
            bindingClassElement,
            pack,
            itemPackage,
            itemClassName,
            annotation.layoutId,
            itemClickListenerType,
            namePrefix
        )
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}
