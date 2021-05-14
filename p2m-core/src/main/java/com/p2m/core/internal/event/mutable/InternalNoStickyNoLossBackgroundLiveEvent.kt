package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoStickyNoLossBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MNoStickyNoLossBackgroundLiveEvent

/**
 * [InternalNoStickyNoLossBackgroundLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoStickyNoLoss] and [observeForeverNoStickyNoLoss] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoStickyNoLossBackgroundLiveEvent<T> : NoStickyNoLossBackgroundLiveEvent<T>,
    P2MNoStickyNoLossBackgroundLiveEvent<T> {

    /**
     * Creates a InternalNoStickyNoLossBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoStickyNoLossBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()
}