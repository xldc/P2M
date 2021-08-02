package com.p2m.annotation.module.api

/**
 * Class annotated by [Service] will generate a interface for service and
 * provide to dependant module.
 *
 * A module has only one.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Service