package com.p2m.core

import android.content.Context
import com.p2m.core.internal.InternalP2MDriver
import com.p2m.core.internal.module.DefaultModuleManager
import com.p2m.core.internal.module.InnerModuleManager
import com.p2m.core.module.ModuleApi

object P2M {
    private var context: Context? = null
    private lateinit var p2mConfigManager: P2MConfigManager
    internal val innerModuleManager: InnerModuleManager by lazy(LazyThreadSafetyMode.PUBLICATION) {
        InnerModuleManager(
            DefaultModuleManager,
            DefaultModuleManager
        )
    }

    /**
     * Get a config manager.
     */
    @JvmStatic
    fun getConfigManager(): P2MConfigManager {
        if (::p2mConfigManager.isInitialized) {
            return p2mConfigManager
        }
        return P2MConfigManager.newInstance().also { this.p2mConfigManager = it }
    }

    /**
     * Create a driver builder.
     *
     * Call P2M.driverBuilder().build().open().
     *
     */
    @JvmStatic
    fun driverBuilder(context: Context): P2MDriver.Builder {
        val applicationContext = context.applicationContext
        P2M.context = applicationContext
        return P2MDriver.Builder(applicationContext, innerModuleManager)
    }

    @JvmStatic
    fun getDriverState(): P2MDriverState = InternalP2MDriver

    /**
     * Get a module of [clazz].
     *
     * @param clazz its class name is defined module name in settings.gradle.
     */
    @JvmStatic
    fun <MODULE_API : ModuleApi<*, *, *>> moduleOf(clazz: Class<MODULE_API>): MODULE_API {
        check(!getDriverState().opening) { "P2M driver is opening. At this time, you can get a module only by call SafeModuleProvider.moduleOf()." }
        check(getDriverState().opened) { "Must call P2M.driverBuilder().build().open() before when call here." }
        return innerModuleManager.moduleOf(clazz)
    }

    @JvmStatic
    inline fun <reified MODULE_API : ModuleApi<*, *, *>> moduleOf(): MODULE_API {
        return moduleOf(MODULE_API::class.java)
    }

    @JvmStatic
    fun getContext(): Context {
        check(getDriverState().opened) { "Must call P2M.driverBuilder().build().open() before when call here." }
        return context!!
    }
}