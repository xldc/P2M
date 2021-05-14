package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoStickyLiveEvent
import com.p2m.core.event.mutable.P2MNoStickyLiveEvent

/**
 * [InternalNoStickyLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoSticky] and [observeForeverNoSticky] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoStickyLiveEvent<T> : NoStickyLiveEvent<T>,
    P2MNoStickyLiveEvent<T> {

    /**
     * Creates a InternalNoStickyLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoStickyLiveEvent with no value assigned to it.
     */
    constructor() : super()
}