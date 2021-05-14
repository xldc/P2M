package com.p2m.annotation.module.api

/**
 * Class annotated by [Launcher] will generate a launch function for launcher and provide
 * to dependant module.
 *
 * Supports: Activity, Fragment, Service
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Launcher