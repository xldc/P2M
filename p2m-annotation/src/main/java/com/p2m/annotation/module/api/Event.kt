package com.p2m.annotation.module.api

/**
 * Class annotated by [Event], it will generate a event interface for the module and
 * provide to dependant module, member property of the class can use [EventField]
 * annotation, will set default [EventField] if no annotated.
 *
 * A module has only one.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Event

/**
 * Field annotated by [EventField], it will generate different event holder property
 * according to different [eventOn] and [eventObservation].
 *
 * Default is [EventOn.MAIN] & [EventObservation.LIKE_LIVE_DATA].
 *
 * Use only in class annotated by [Event].
 *
 * @property eventOn specified thread to receive event.
 * @property eventObservation specified receive event function.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class EventField(
    val eventOn: EventOn = EventOn.MAIN,
    val eventObservation: EventObservation = EventObservation.LIKE_LIVE_DATA
)

/**
 * Which thread to manage and dispatch event do you want.
 */
enum class EventOn{
    MAIN,               // receive event on main thread
    BACKGROUND,         // receive event on background thread, not occupy main thread resources
}

/**
 * Which observation do you want for [EventField].
 *
 * See [live-event library](https://github.com/wangdaqi77/live-event/)
 */
enum class EventObservation{
    LIKE_LIVE_DATA,     // like LiveData, sticky and loss
    NO_STICKY,          // no sticky a old event
    NO_LOSS,            // no loss every event
    NO_STICKY_NO_LOSS,  // NO_STICKY + NO_LOSS
    MIXED,              // include all of the above
}