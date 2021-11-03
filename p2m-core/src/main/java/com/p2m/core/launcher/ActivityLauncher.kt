package com.p2m.core.launcher

import android.content.Context
import android.content.Intent
import com.p2m.core.internal.launcher.InternalActivityLauncher
import kotlin.reflect.KProperty

interface ActivityLauncher {
    class Delegate(clazz: Class<*>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) { InternalActivityLauncher(clazz) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityLauncher = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ActivityLauncher) = Unit
    }

    fun createIntent(context: Context): Intent
}