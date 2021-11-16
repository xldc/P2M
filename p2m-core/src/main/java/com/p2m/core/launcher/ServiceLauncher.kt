package com.p2m.core.launcher

import android.content.Context
import android.content.Intent
import android.app.Service
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.core.internal.launcher.InternalServiceLauncher
import kotlin.reflect.KProperty

/**
 * A launcher of Service.
 *
 * @see ApiLauncher
 */
interface ServiceLauncher {

    class Delegate(clazz: Class<*>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) { InternalServiceLauncher(clazz) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ServiceLauncher = real
    }

    /**
     * Create a instance of Intent for that [Service] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with explicit calls.
     *
     * @return a instance of Intent.
     */
    fun createIntent(context: Context): Intent
}