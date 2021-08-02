package com.p2m.core.internal.event

import com.p2m.core.event.LiveEvent
import wang.lifecycle.MutableLiveEvent

/**
 * [InternalLiveEvent] publicly exposes all observe method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalLiveEvent<T> : MutableLiveEvent<T>, LiveEvent<T> {

    /**
     * Creates a InternalMixedLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalMixedLiveEvent with no value assigned to it.
     */
    constructor() : super()
}