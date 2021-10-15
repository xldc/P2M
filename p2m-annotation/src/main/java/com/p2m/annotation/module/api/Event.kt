package com.p2m.annotation.module.api

/**
 * Class annotated by [Event], it will generate a event interface for the module and
 * provide to dependant module, member property of the class is effective that use
 * [EventField] annotation.
 *
 * A module has only one.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Event

/**
 * Field annotated by [EventField], it will generate different event holder property
 * according to [eventOn] and [mutableFromExternal].
 *
 * Default is [EventOn.MAIN] + immutable from external.
 *
 * Use only in class annotated by [Event].
 *
 * @property eventOn specified thread to receive event.
 * @property mutableFromExternal mutable from external, if true that dependant module
 * support call setValue and postValue.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class EventField(val eventOn: EventOn = EventOn.MAIN, val mutableFromExternal: Boolean = false)

/**
 * Which thread to manage and dispatch event do you want.
 */
enum class EventOn{
    MAIN,               // receive event on main thread
    BACKGROUND,         // receive event on background thread, not occupy main thread resources
}