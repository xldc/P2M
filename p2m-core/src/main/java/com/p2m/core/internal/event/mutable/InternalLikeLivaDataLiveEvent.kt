package com.p2m.core.internal.event.mutable

import androidx.lifecycle.LiveData
import androidx.lifecycle.mutable.LikeLivaDataLiveEvent
import com.p2m.core.event.mutable.P2MLikeLiveDataLiveEvent

/**
 * [InternalLikeLivaDataLiveEvent] like [LiveData].
 *
 * @param T The type of data hold by this instance
 */
internal class InternalLikeLivaDataLiveEvent<T> : LikeLivaDataLiveEvent<T>,
    P2MLikeLiveDataLiveEvent<T> {

    /**
     * Creates a InternalLikeLivaDataLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalLikeLivaDataLiveEvent with no value assigned to it.
     */
    constructor() : super()
}