package com.databinding.annotations

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class BindAction(
    val paths: Array<String>
    )