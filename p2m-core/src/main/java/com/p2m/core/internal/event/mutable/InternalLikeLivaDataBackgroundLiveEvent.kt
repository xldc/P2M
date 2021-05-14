package com.p2m.core.internal.event.mutable

import androidx.lifecycle.LiveData
import androidx.lifecycle.mutable.LikeLivaDataBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MLikeLiveDataBackgroundLiveEvent

/**
 * [InternalLikeLivaDataBackgroundLiveEvent] like [LiveData].
 *
 * @param T The type of data hold by this instance
 */
internal class InternalLikeLivaDataBackgroundLiveEvent<T> : LikeLivaDataBackgroundLiveEvent<T>,
    P2MLikeLiveDataBackgroundLiveEvent<T> {

    /**
     * Creates a InternalLikeLivaDataBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalLikeLivaDataBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()
}