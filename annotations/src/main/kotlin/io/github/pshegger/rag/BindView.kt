package io.github.pshegger.rag

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class BindView(val viewId: Int)
