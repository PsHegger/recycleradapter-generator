package io.github.pshegger.rag

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModelBinding(
    val layoutId: Int,
    val clickListener: Boolean = false,
    val namePrefix: String = ""
)
