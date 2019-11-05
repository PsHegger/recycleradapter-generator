package io.github.pshegger.rag.builders

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.pshegger.rag.utils.AdapterInfo
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.ProcessingEnvironment

class AdapterBuilder(
    private val processingEnv: ProcessingEnvironment,
    private val info: AdapterInfo
) {

    private val adapterClassName = "${info.namePrefix}Adapter"
    private val viewHolderClassName = ClassName(
        info.pack,
        "$adapterClassName.${info.namePrefix}ViewHolder"
    )
    private val itemListClassName =
        ClassName("kotlin.collections", "List")
            .parameterizedBy(ClassName(info.itemPackage, info.itemClassName))

    private fun TypeSpec.Builder.addItemClickListener() = apply {
        if (info.itemClickListenerType == null) return@apply

        addProperty(
            PropertySpec.builder(
                "itemClickListener",
                info.itemClickListenerType.copy(nullable = true),
                KModifier.PRIVATE
            )
                .mutable()
                .initializer("null")
                .build()
        )

        addProperty(
            PropertySpec.builder(
                "defaultItemClickListener",
                info.itemClickListenerType,
                KModifier.PRIVATE
            )
                .initializer("{ itemClickListener?.invoke(it) }")
                .build()
        )

        addFunction(
            FunSpec.builder("setOnItemClickListener")
                .addParameter("listener", info.itemClickListenerType.copy(nullable = false))
                .addStatement("itemClickListener = listener")
                .build()
        )
    }

    private fun TypeSpec.Builder.addOverriddenMethods() = apply {
        addFunction(
            FunSpec.builder("getItemCount")
                .returns(INT)
                .addStatement("return items.size")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )

        addFunction(
            FunSpec.builder("onBindViewHolder")
                .addParameter("viewHolder", viewHolderClassName)
                .addParameter("position", INT)
                .addStatement("viewHolder.bind(items[position])")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )

        addFunction(
            FunSpec.builder("onCreateViewHolder")
                .returns(viewHolderClassName)
                .addParameter(
                    "parent",
                    ClassName("android.view", "ViewGroup")
                )
                .addParameter("viewType", INT)
                .addStatement(
                    "val view = android.view.LayoutInflater.from(parent.context).inflate(%L, parent, false)",
                    info.layoutId
                )
                .apply {
                    if (info.itemClickListenerType != null) {
                        addStatement("return ${info.namePrefix}ViewHolder(view, defaultItemClickListener)")
                    } else {
                        addStatement("return ${info.namePrefix}ViewHolder(view)")
                    }
                }
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
    }

    fun build() = TypeSpec.classBuilder(adapterClassName)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("items", itemListClassName)
                .build()
        )
        .superclass(
            ClassName("androidx.recyclerview.widget.RecyclerView", "Adapter")
                .parameterizedBy(viewHolderClassName)
        )
        .addProperty(
            PropertySpec.builder("items", itemListClassName)
                .initializer("items")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addItemClickListener()
        .addOverriddenMethods()
        .addType(ViewHolderBuilder(processingEnv, info).build())
        .build()
}
