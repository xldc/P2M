package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoLossBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MNoLossBackgroundLiveEvent

/**
 * [InternalNoLossBackgroundLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoLoss] and [observeForeverNoLoss] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoLossBackgroundLiveEvent<T> : NoLossBackgroundLiveEvent<T>,
    P2MNoLossBackgroundLiveEvent<T> {

    /**
     * Creates a InternalNoLossBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoLossBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()
}