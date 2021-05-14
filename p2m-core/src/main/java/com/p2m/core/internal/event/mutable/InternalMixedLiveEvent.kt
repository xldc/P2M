package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.MixedLiveEvent
import com.p2m.core.event.mutable.P2MMixedLiveEvent

/**
 * [InternalMixedLiveEvent] publicly exposes all observe method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalMixedLiveEvent<T> : MixedLiveEvent<T>,
    P2MMixedLiveEvent<T> {

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