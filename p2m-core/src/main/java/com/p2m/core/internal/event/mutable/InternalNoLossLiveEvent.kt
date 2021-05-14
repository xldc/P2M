package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoLossLiveEvent
import com.p2m.core.event.mutable.P2MNoLossLiveEvent

/**
 * [InternalNoLossLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoLoss] and [observeForeverNoLoss] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoLossLiveEvent<T> : NoLossLiveEvent<T>,
    P2MNoLossLiveEvent<T> {

    /**
     * Creates a InternalNoLossLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoLossLiveEvent with no value assigned to it.
     */
    constructor() : super()
}