package com.p2m.core.launcher

import android.app.Fragment
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.core.internal.launcher.InternalFragmentLauncher
import kotlin.reflect.KProperty

interface FragmentLauncher<T> {

    class Delegate<T>(createBlock:() -> T) {
        private val real by lazy(LazyThreadSafetyMode.NONE) { InternalFragmentLauncher<T>(createBlock) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): FragmentLauncher<T> = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: FragmentLauncher<T>) = Unit
    }

    /**
     * Create a instance for that [Fragment] class annotated by [ApiLauncher].
     *
     * @return a instance.
     */
    fun create(): T
}