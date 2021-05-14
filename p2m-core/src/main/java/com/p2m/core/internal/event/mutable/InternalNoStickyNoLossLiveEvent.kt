package com.p2m.core.internal.event.mutable

import androidx.lifecycle.mutable.NoStickyNoLossLiveEvent
import com.p2m.core.event.mutable.P2MNoStickyNoLossLiveEvent

/**
 * [InternalNoStickyNoLossLiveEvent] which override [observe] and [observeForever] method,
 * they actually map [observeNoStickyNoLoss] and [observeForeverNoStickyNoLoss] method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalNoStickyNoLossLiveEvent<T> : NoStickyNoLossLiveEvent<T>,
    P2MNoStickyNoLossLiveEvent<T> {

    /**
     * Creates a InternalNoStickyNoLossLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalNoStickyNoLossLiveEvent with no value assigned to it.
     */
    constructor() : super()
}