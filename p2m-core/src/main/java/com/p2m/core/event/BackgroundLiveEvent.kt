package com.p2m.core.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.p2m.annotation.module.api.*
import com.p2m.core.internal.event.InternalBackgroundLiveEvent
import com.p2m.core.internal.event.InternalBackgroundObserver
import kotlin.reflect.KProperty

/**
 * Defined a common interface for background event holder class.
 *
 * See [live-event library](https://github.com/wangdaqi77/live-event)
 *
 * @see EventOn.BACKGROUND - gen by KAPT.
 * @see BackgroundObserver - can specified thread to receive event.
 */
interface BackgroundLiveEvent<T> {

    class Delegate<T> {
        private val real by lazy { InternalBackgroundLiveEvent<T>() }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): BackgroundLiveEvent<T> = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: BackgroundLiveEvent<T>) = Unit
    }

    fun observe(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForever(observer: Observer<in T>)

    fun observeNoSticky(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForeverNoSticky(observer: Observer<in T>)

    fun observeNoLoss(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForeverNoLoss(observer: Observer<in T>)

    fun observeNoStickyNoLoss(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForeverNoStickyNoLoss(observer: Observer<in T>)

    fun removeObservers(owner: LifecycleOwner)

    fun removeObserver(observer: Observer<in T>)

    fun hasActiveObservers(): Boolean

    fun hasObservers(): Boolean

    fun postValue(value: T)

    fun setValue(value: T)

    fun getValue(): T?
}

enum class ObserveOn {
    BACKGROUND,     // Receiving event in background thread, not recommended time-consuming work.
    ASYNC,          // Receiving event in thread pool.
    MAIN            // Receiving event in main thread, not recommended time-consuming work.
}

/**
 * A simple callback that can specified thread to receive event from [BackgroundLiveEvent].
 */
abstract class BackgroundObserver<T>(observeOn: ObserveOn) : Observer<T> {
    internal val real = object : InternalBackgroundObserver<T>(observeOn) {
        override fun onChanged(t: T) {
            this@BackgroundObserver.onChanged(t)
        }
    }
}