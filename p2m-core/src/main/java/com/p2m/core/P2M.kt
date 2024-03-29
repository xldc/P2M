package com.p2m.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.MainThread
import com.p2m.core.app.App
import com.p2m.core.config.P2MConfigManager
import com.p2m.core.internal.config.InternalP2MConfigManager
import com.p2m.core.internal.moduleName
import com.p2m.core.internal.module.deriver.InternalDriver
import com.p2m.core.internal.module.DefaultModuleCollectorFactory
import com.p2m.core.internal.module.DefaultModuleFactory
import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.internal.module.ManifestModuleFinder
import com.p2m.core.module.*

@SuppressLint("StaticFieldLeak")
object P2M : ModuleApiProvider{
    internal lateinit var internalContext : Context
    private lateinit var moduleCollector : ModuleCollector
    private lateinit var driver: InternalDriver
    private lateinit var manifestModuleFinder : ManifestModuleFinder
    private val moduleFactory: ModuleFactory = DefaultModuleFactory()
    private val moduleContainer = ModuleContainerImpl()
    internal val configManager: P2MConfigManager = InternalP2MConfigManager()

    /**
     * Start a config.
     */
    fun config(block: P2MConfigManager.() -> Unit) {
        block(this.configManager)
    }

    /**
     * Initialization.
     */
    @MainThread
    fun init(context: Context, vararg externalModuleName: String) {
        check(!this::internalContext.isInitialized) { "`P2M.init()` can only be called once." }
        check(Looper.getMainLooper() === Looper.myLooper()) { "`P2M.init()` must be called on the main thread." }
        val app = App()
        val applicationContext = context.applicationContext
        this.internalContext = applicationContext
        this.manifestModuleFinder = ManifestModuleFinder(applicationContext)
        this.moduleCollector = DefaultModuleCollectorFactory()
            .newInstance("${applicationContext.packageName}.ModuleAutoCollector")
            .apply {
                injectForCreatedModule(app, moduleContainer)
                injectForExternal(manifestModuleFinder, moduleFactory, moduleContainer, *externalModuleName)
                injectForAllFromTop(app, manifestModuleFinder, moduleFactory, moduleContainer)
            }
        this.driver = InternalDriver(applicationContext, app, this.moduleContainer)
        this.driver.considerOpenAwait()
    }

    /**
     * Get instance of `api` by [clazz] of module.
     *
     * @param clazz its class name is defined module name in `settings.gradle`.
     *
     * @see Module
     * @see ModuleApi
     */
    override fun <MODULE_API : ModuleApi<*, *, *>> apiOf(
        clazz: Class<out Module<MODULE_API>>
    ): MODULE_API {
        check(::internalContext.isInitialized) { "Must call P2M.init() before when call here." }

        val driver = this.driver
        check(driver.isEvaluating?.get() != true) { "Don not call `P2M.apiOf()` in `onEvaluate()`." }
        driver.safeModuleApiProvider?.get()?.let { moduleProvider ->
            return moduleProvider.apiOf(clazz)
        }

        val module = moduleContainer.find(clazz)
        check(module != null) { "The ${clazz.moduleName} is not exist for ${clazz.name}" }
        driver.considerOpenAwait()
        @Suppress("UNCHECKED_CAST")
        return module.api as MODULE_API
    }
}