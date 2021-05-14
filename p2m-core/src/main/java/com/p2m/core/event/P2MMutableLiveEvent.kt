package com.p2m.core.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * Defined a common interface for use mutable event holder class.
 */
interface P2MMutableLiveEvent<T>{
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)

    fun observeForever(observer: Observer<in T>)

    fun removeObservers(owner: LifecycleOwner)

    fun removeObserver(observer: Observer<in T>)

    fun hasActiveObservers(): Boolean

    fun hasObservers(): Boolean

    fun postValue(value: T)

    fun setValue(value: T)

    fun getValue(): T?
}
