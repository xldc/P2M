package com.p2m.core.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.p2m.core.internal.event.*
import com.p2m.annotation.module.api.*

/**
 * Defined a common interface for use mutable background event holder class.
 */
interface P2MMutableBackgroundLiveEvent<T>{
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
