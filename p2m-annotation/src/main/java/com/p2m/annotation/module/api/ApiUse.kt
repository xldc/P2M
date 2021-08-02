package com.p2m.annotation.module.api

/**
 * Class annotated by [ApiUse] will provide to dependant module.
 *
 * NOTE: recommended only for data classes.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiUse