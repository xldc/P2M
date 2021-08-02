package com.p2m.core.internal.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.p2m.core.event.BackgroundLiveEvent
import com.p2m.core.event.BackgroundObserver
import com.p2m.core.event.ObserveOn
import wang.lifecycle.EventDispatcher
import wang.lifecycle.MutableBackgroundLiveEvent


/**
 * [InternalBackgroundLiveEvent] publicly exposes all observe method.
 *
 * @param T The type of data hold by this instance
 */
internal class InternalBackgroundLiveEvent<T> : MutableBackgroundLiveEvent<T>, BackgroundLiveEvent<T> {

    /**
     * Creates a InternalMixedBackgroundLiveEvent initialized with the given value.
     *
     * @property value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a InternalMixedBackgroundLiveEvent with no value assigned to it.
     */
    constructor() : super()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observe(owner, it)
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeForever(it)
        }
    }

    override fun observeNoSticky(owner: LifecycleOwner, observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeNoSticky(owner, it)
        }
    }

    override fun observeForeverNoSticky(observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeForeverNoSticky(it)
        }
    }

    override fun observeNoLoss(owner: LifecycleOwner, observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeNoLoss(owner, it)
        }
    }
    override fun observeForeverNoLoss(observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeForeverNoLoss(it)
        }
    }

    override fun observeNoStickyNoLoss(owner: LifecycleOwner, observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeNoStickyNoLoss(owner, it)
        }
    }

    override fun observeForeverNoStickyNoLoss(observer: Observer<in T>) {
        observer.transformRealObserver {
            super.observeForeverNoStickyNoLoss(it)
        }
    }

    override fun removeObserver(observer: Observer<in T>) {
        observer.transformRealObserver {
            super.removeObserver(it)
        }
    }

    private fun Observer<in T>.transformRealObserver(block: (observer: Observer<in T>) -> Unit) {
        if (this is BackgroundObserver) {
            block(real)
            return
        }
        block(this)
    }
}

internal open class InternalBackgroundObserver<T>(observeOn: ObserveOn) : wang.lifecycle.BackgroundObserver<T>(
    dispatcher = when (observeOn) {
        ObserveOn.BACKGROUND -> EventDispatcher.BACKGROUND
        ObserveOn.ASYNC -> EventDispatcher.ASYNC
        ObserveOn.MAIN -> EventDispatcher.MAIN
    }

) { override fun onChanged(t: T) {} }