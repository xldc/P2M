package com.p2m.core.event.mutable

import com.p2m.annotation.module.api.*
import com.p2m.core.event.P2MMutableLiveEvent
import com.p2m.core.internal.event.mutable.InternalNoLossLiveEvent
import kotlin.reflect.KProperty

/**
 * Defined a interface for use event holder class of
 * [EventOn.MAIN] & [EventObservation.NO_LOSS].
 *
 * See [live-event library](https://github.com/wangdaqi77/live-event)
 */
interface P2MNoLossLiveEvent<T> : P2MMutableLiveEvent<T> {

    class Delegate<T> {
        private val real by lazy(LazyThreadSafetyMode.PUBLICATION) {
            InternalNoLossLiveEvent<T>()
        }

        operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): P2MNoLossLiveEvent<T> = real

        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: P2MNoLossLiveEvent<T>
        ) = Unit
    }

}
