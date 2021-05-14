package com.p2m.core.event.mutable

import androidx.lifecycle.*

import com.p2m.annotation.module.api.EventObservation
import com.p2m.annotation.module.api.EventOn
import com.p2m.core.event.P2MMutableLiveEvent
import com.p2m.core.internal.event.mutable.InternalMixedLiveEvent
import kotlin.reflect.KProperty

/**
 * Defined a interface for use event holder class of
 * [EventOn.MAIN] & [EventObservation.MIXED].
 *
 * See [live-event library](https://github.com/wangdaqi77/live-event)
 */
interface P2MMixedLiveEvent<T> : P2MMutableLiveEvent<T> {

    fun observeNoSticky(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForeverNoSticky(observer: Observer<in T>) 

    fun observeNoLoss(owner: LifecycleOwner, observer: Observer<in T>) 

    fun observeForeverNoLoss(observer: Observer<in T>) 

    fun observeNoStickyNoLoss(owner: LifecycleOwner, observer: Observer<in T>) 

    fun observeForeverNoStickyNoLoss(observer: Observer<in T>)

    class Delegate<T> {
        private val real by lazy(LazyThreadSafetyMode.PUBLICATION) {
            InternalMixedLiveEvent<T>()
        }

        operator fun getValue(
            thisRef: Any?,
            property: KProperty<*>
        ): P2MMixedLiveEvent<T> = real

        operator fun setValue(
            thisRef: Any?,
            property: KProperty<*>,
            value: P2MMixedLiveEvent<T>
        ) = Unit
    }

}