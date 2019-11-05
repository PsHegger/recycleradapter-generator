package io.github.pshegger.rag.utils

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.element.Element

fun getClassName(element: Element): ClassName {
    val splitName = element.asType().toString().split(".")
    val pack = splitName.dropLast(1).joinToString(".")
    val className = splitName.last()
    return ClassName(pack, className)
}
