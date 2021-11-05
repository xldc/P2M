package com.p2m.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.MainThread
import com.p2m.core.app.App
import com.p2m.core.config.P2MConfigManager
import com.p2m.core.internal.config.InternalP2MConfigManager
import com.p2m.core.internal.module.deriver.InternalDriver
import com.p2m.core.internal.module.DefaultModuleCollectorFactory
import com.p2m.core.internal.module.DefaultModuleFactory
import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.module.*

@SuppressLint("StaticFieldLeak")
object P2M : ModuleApiProvider{
    private lateinit var context : Context
    private lateinit var moduleCollector : ModuleCollector
    private lateinit var driver: InternalDriver
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
    fun init(context: Context) {
        check(!this::context.isInitialized) { "P2M.init() can only be called once." }
        check(Looper.getMainLooper() === Looper.myLooper()) { "P2M.init() must be called on the main thread." }
        val app = App()
        val applicationContext = context.applicationContext
        this.context = applicationContext
        this.moduleCollector = DefaultModuleCollectorFactory().newInstance("${applicationContext.packageName}.ModuleAutoCollector")
        prepareModule(app)
        this.driver = InternalDriver(applicationContext, app, this.moduleContainer)
        this.driver.considerOpenAwait()
    }

    private fun prepareModule(app: App) {
        this.moduleCollector.injectFrom(app, moduleFactory, moduleContainer) {
            app.internalModuleUnit.dependOn(
                it.internalModuleUnit.modulePublicClass,
                it.internalModuleUnit.moduleImplClass
            )
        }
    }

    /**
     * Get a module api by [clazz] of module.
     *
     * @param clazz its class name is defined module name in settings.gradle.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <MODULE_API : ModuleApi<*, *, *>, MODULE : Module<MODULE_API>> moduleApiOf(
        clazz: Class<MODULE>
    ): MODULE_API {
        check(::context.isInitialized) { "Must call P2M.init() before when call here." }

        val driver = this.driver
        check(driver.isEvaluating?.get() != true) { "Don not call P2M.moduleApiOf() in onEvaluate()." }
        driver.executingModuleProvider?.get()?.let { moduleProvider ->
            return moduleProvider.moduleApiOf(clazz)
        }

        val module = moduleContainer.find(clazz)
        check(module != null) { "The ${clazz.moduleName} is not exist for ${clazz.name}" }
        driver.considerOpenAwait()
        return module.api as MODULE_API
    }

    private inline val Class<out Module<*>>.moduleName: String
        get() = simpleName.removePrefix("_")
}