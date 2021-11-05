package com.p2m.annotation.module.api

/**
 * A class uses this annotation will extract to Api area to provide to dependant module.
 *
 * NOTE: recommended only for data classes.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ApiUse