package com.p2m.annotation.module

/**
 * A class uses this annotation for module initialization of Module Init area.
 *
 * Use `P2M.init()` to start to initialize all modules.
 *
 * The annotation can only be used once within same module.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ModuleInitializer