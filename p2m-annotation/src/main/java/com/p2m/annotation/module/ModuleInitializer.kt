package com.p2m.annotation.module

/**
 * Class annotated by [ModuleInitializer] for the module initialization.
 *
 * A module has only one.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ModuleInitializer