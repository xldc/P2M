package com.p2m.core

import android.content.Context
import com.p2m.core.config.P2MConfigManager
import com.p2m.core.driver.P2MDriverBuilder
import com.p2m.core.driver.P2MDriverState
import com.p2m.core.internal.config.InternalP2MConfigManager
import com.p2m.core.internal.deriver.InternalP2MDriver
import com.p2m.core.internal.deriver.InternalP2MDriverBuilder
import com.p2m.core.internal.module.DefaultModuleManager
import com.p2m.core.internal.module.InnerModuleManager
import com.p2m.core.module.ModuleApi

object P2M {
    private var context: Context? = null
    private lateinit var p2mConfigManager: P2MConfigManager
    private val innerModuleManager: InnerModuleManager by lazy(LazyThreadSafetyMode.NONE) {
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
        return InternalP2MConfigManager().also { this.p2mConfigManager = it }
    }

    /**
     * Start a config.
     */
    fun config(block: P2MConfigManager.() -> Unit) {
        if (!::p2mConfigManager.isInitialized) {
            p2mConfigManager = InternalP2MConfigManager()
        }
        block(p2mConfigManager)
    }

    /**
     * Create a driver builder.
     *
     * Call P2M.driverBuilder().build().open().
     *
     */
    @JvmStatic
    fun driverBuilder(context: Context): P2MDriverBuilder {
        val applicationContext = context.applicationContext
        P2M.context = applicationContext
        return InternalP2MDriverBuilder(applicationContext, innerModuleManager)
    }

    @JvmStatic
    fun getDriverState(): P2MDriverState = InternalP2MDriver

    /**
     * Get a module api of [clazz].
     *
     * @param clazz its class name is defined module name in settings.gradle.
     */
    @JvmStatic
    fun <MODULE_API : ModuleApi<*, *, *>> moduleApiOf(clazz: Class<MODULE_API>): MODULE_API {
        check(!getDriverState().opening) { "P2M driver is opening. At this time, you can get a module only by call SafeModuleProvider.moduleApiOf()." }
        check(getDriverState().opened) { "Must call P2M.driverBuilder().build().open() before when call here." }
        return innerModuleManager.moduleApiOf(clazz)
    }

    @JvmStatic
    inline fun <reified MODULE_API : ModuleApi<*, *, *>> moduleApiOf(): MODULE_API {
        return moduleApiOf(MODULE_API::class.java)
    }

    @JvmStatic
    fun getContext(): Context {
        check(getDriverState().opened) { "Must call P2M.driverBuilder().build().open() before when call here." }
        return context!!
    }
}