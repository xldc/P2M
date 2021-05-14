package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.MixedBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MMixedBackgroundLiveEvent

/**
 * [InternalMixedBackgroundLiveEvent] publicly exposes all observe method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalMixedBackgroundLiveEvent<T> : MixedBackgroundLiveEvent<T>,
    P2MMixedBackgroundLiveEvent<T> {

    /**
     * Creates a InternalMixedBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalMixedBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()
}