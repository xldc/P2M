package com.p2m.core.launcher

import android.content.Context
import android.content.Intent
import android.app.Service
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.core.internal.launcher.InternalServiceLauncher
import kotlin.reflect.KProperty

/**
 * A launcher of `Service`.
 *
 * For example, has a `Service` for work in `Account` module:
 * ```kotlin
 * @ApiLauncher("Work")
 * class WorkService : Service()
 * ```
 *
 * then launch in `activity` of external module:
 * ```kotlin
 * P2M.apiOf(Account)
 *      .launcher
 *      .serviceOfWork
 *      .launchCalling(::startService)
 * ```
 *
 * @see Context.startService - e.g.`launchCalling(::startService)`.
 * @see Context.startForegroundService - e.g.`launchCalling(::startForegroundService)`.
 * @see Context.bindService - declare `bind` method for use `launchCalling(::bind)`.
 * @see Context.unbindService - e.g.`launchCalling(::unbindService)`.
 * @see ApiLauncher
 */
interface ServiceLauncher {

    class Delegate(clazz: Class<*>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) { InternalServiceLauncher(clazz) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ServiceLauncher = real
    }

    /**
     * Launch a service for that [Service] class annotated by [ApiLauncher].
     *
     * [launchBlock] is real launch method, that has a created Intent instance
     * as input param, all other fields (action, data, type) are null,  though
     * they can be modified later with explicit calls.
     *
     */
    fun launchCalling(launchBlock: LaunchServiceBlock)
}

/**
 * A block for launch service.
 */
typealias LaunchServiceBlock = (createdIntent: Intent) -> Unit