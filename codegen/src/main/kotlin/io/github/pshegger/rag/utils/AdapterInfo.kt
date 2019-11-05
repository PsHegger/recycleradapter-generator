package io.github.pshegger.rag.utils

import com.squareup.kotlinpoet.LambdaTypeName
import javax.lang.model.element.Element

data class AdapterInfo(
    val bindingClassElement: Element,
    val pack: String,
    val itemPackage: String,
    val itemClassName: String,
    val layoutId: Int,
    val itemClickListenerType: LambdaTypeName?,
    val namePrefix: String
)
