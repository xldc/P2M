package com.p2m.core.event.mutable

import com.p2m.annotation.module.api.*
import com.p2m.core.event.P2MMutableBackgroundLiveEvent
import com.p2m.core.internal.event.mutable.InternalNoLossBackgroundLiveEvent
import kotlin.reflect.KProperty

/**
 * Defined a interface for use event holder class of
 * [EventOn.BACKGROUND] & [EventObservation.NO_LOSS].
 *
 * See [live-event library](https://github.com/wangdaqi77/live-event)
 */
interface P2MNoLossBackgroundLiveEvent<T> :
    P2MMutableBackgroundLiveEvent<T> {

    class Delegate<T> {
        private val real by lazy(LazyThreadSafetyMode.PUBLICATION) {
            InternalNoLossBackgroundLiveEvent<T>()
        }

        operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): P2MNoLossBackgroundLiveEvent<T> = real

        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: P2MNoLossBackgroundLiveEvent<T>
        ) = Unit
    }

}