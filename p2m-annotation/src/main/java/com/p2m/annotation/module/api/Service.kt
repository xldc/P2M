package com.p2m.annotation.module.api

/**
 * A class uses this annotation will generate a interface for service of Api area and
 * provide to dependant module.
 *
 * Use `P2M.moduleApiOf(${moduleName}::class.java).service` to get service, that `moduleName`
 * is defined in settings.gradle
 *
 *
 * The annotation can only be used once within same module.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Service