package com.p2m.core

import android.app.Application
import android.content.Context
import com.p2m.core.app.App
import com.p2m.core.config.P2MConfigManager
import com.p2m.core.driver.P2MDriverBuilder
import com.p2m.core.driver.P2MDriverState
import com.p2m.core.internal.config.InternalP2MConfigManager
import com.p2m.core.internal.deriver.InternalP2MDriverBuilder
import com.p2m.core.internal.module.DefaultModuleCollectorFactory
import com.p2m.core.internal.module.DefaultModuleFactory
import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.module.*

object P2M : ModuleApiProvider{
    internal val configManager: P2MConfigManager = InternalP2MConfigManager()
    private var driverState = P2MDriverState()
    private val moduleContainer by lazy { ModuleContainerImpl(App::class.java, DefaultModuleFactory()) }

    /**
     * Start a config.
     */
    fun config(block: P2MConfigManager.() -> Unit) {
        block(configManager)
    }

    /**
     * Create a driver builder.
     *
     * Call P2M.driverBuilder().build().open().
     *
     */
    fun driverBuilder(context: Context): P2MDriverBuilder {
        check(!driverState.opening) { "P2M driver is opening." }
        check(!driverState.opened) { "P2M driver been opened." }
        val application = context.applicationContext as Application
        val moduleAutoCollector = DefaultModuleCollectorFactory().newInstance("${application.packageName}.ModuleAutoCollector")
        moduleAutoCollector.inject(moduleContainer)
        return InternalP2MDriverBuilder(application, moduleContainer, driverState)
    }

    fun getDriverState(): P2MDriverState = driverState

    /**
     * Get a module api of [clazz].
     *
     * @param clazz its class name is defined module name in settings.gradle.
     */
    override fun <MODULE_API : ModuleApi<*, *, *>, MODULE : Module<MODULE_API, *>> moduleApiOf(
        clazz: Class<MODULE>
    ): MODULE_API {
        check(!driverState.opening) { "P2M driver is opening. At this time, you can get a module only by call SafeModuleApiProvider.moduleApiOf()." }
        check(driverState.opened) { "Must call P2M.driverBuilder().build().open() before when call here." }
        @Suppress("UNCHECKED_CAST")
        return moduleContainer.find(clazz)?.module?.api as MODULE_API
    }

    inline fun <reified MODULE_API : ModuleApi<*, *, *>, reified MODULE : Module<MODULE_API, *>> moduleApiOf(): MODULE_API {
        return moduleApiOf(MODULE::class.java)
    }
}