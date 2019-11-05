package io.github.pshegger.rag.builders

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.pshegger.rag.BindView
import io.github.pshegger.rag.utils.AdapterInfo
import io.github.pshegger.rag.utils.getClassName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.tools.Diagnostic

class ViewHolderBuilder(
    private val processingEnv: ProcessingEnvironment,
    private val info: AdapterInfo
) {

    private val viewHolderClassName = "${info.namePrefix}ViewHolder"

    private fun TypeSpec.Builder.addItemClickListener() = apply {
        if (info.itemClickListenerType == null) return@apply

        addProperty(
            PropertySpec.builder("onClickListener", info.itemClickListenerType, KModifier.PRIVATE)
                .initializer("onClickListener")
                .build()
        )
    }

    private fun TypeSpec.Builder.addView(name: String, className: ClassName, viewId: Int) = apply {
        addProperty(
            PropertySpec.builder(name, className, KModifier.PRIVATE)
                .initializer("itemView.findViewById(%L)", viewId)
                .build()
        )
    }

    private fun TypeSpec.Builder.addBindings() = apply {
        val bindFuncBuilder = FunSpec.builder("bind")
            .addParameter("item", ClassName(info.itemPackage, info.itemClassName))

        val bindingClassName = getClassName(info.bindingClassElement)
        bindFuncBuilder.addStatement("val binder = ${bindingClassName.canonicalName}(item)")

        info.bindingClassElement.enclosedElements
            .filter { it.getAnnotation(BindView::class.java) != null && it.kind == ElementKind.METHOD }
            .map { it as ExecutableElement }
            .forEach { bindingMethod ->
                if (bindingMethod.parameters.size != 1) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.WARNING,
                        "Skipping ${bindingMethod.asType()}: binding methods should contain exactly one parameter"
                    )
                    return@forEach
                }

                val bind = bindingMethod.getAnnotation(BindView::class.java)
                val p = bindingMethod.parameters[0]
                val viewName = p.simpleName.toString()

                addView(viewName, getClassName(p), bind.viewId)
                bindFuncBuilder.addStatement("binder.${bindingMethod.simpleName}($viewName)")
            }

        if (info.itemClickListenerType != null) {
            bindFuncBuilder.addStatement("itemView.setOnClickListener { onClickListener(adapterPosition) }")
        }

        addFunction(bindFuncBuilder.build())
    }

    fun build() = TypeSpec.classBuilder(viewHolderClassName)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("itemView", ClassName("android.view", "View"))
                .apply {
                    if (info.itemClickListenerType != null) {
                        addParameter("onClickListener", info.itemClickListenerType)
                    }
                }
                .build()
        )
        .superclass(ClassName("androidx.recyclerview.widget.RecyclerView", "ViewHolder"))
        .addSuperclassConstructorParameter("itemView")
        .addItemClickListener()
        .addBindings()
        .build()
}
