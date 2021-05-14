package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoStickyBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MNoStickyBackgroundLiveEvent

/**
 * [InternalNoStickyBackgroundLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoSticky] and [observeForeverNoSticky] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoStickyBackgroundLiveEvent<T> : NoStickyBackgroundLiveEvent<T>,
    P2MNoStickyBackgroundLiveEvent<T> {

    /**
     * Creates a InternalNoStickyBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoStickyBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()
}