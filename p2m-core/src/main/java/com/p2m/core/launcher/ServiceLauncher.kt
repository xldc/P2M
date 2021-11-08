package com.p2m.core.launcher

import android.content.Context
import android.content.Intent
import android.app.Service
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.core.internal.launcher.InternalServiceLauncher
import kotlin.reflect.KProperty

interface ServiceLauncher {

    class Delegate(clazz: Class<*>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) { InternalServiceLauncher(clazz) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ServiceLauncher = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ServiceLauncher) = Unit
    }

    /**
     * Create a instance of Intent for that [Service] class annotated by [ApiLauncher].
     *
     * @return a instance of Intent.
     */
    fun createIntent(context: Context): Intent
}