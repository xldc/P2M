package com.p2m.core.module

abstract class Module<MODULE_API : ModuleApi<*, *, *>, MODULE_INIT : ModuleInit> {
    abstract val api: MODULE_API
    protected abstract val init: MODULE_INIT
    protected var _apiClazz: Class<out Module<*, *>>? = null
    @Suppress("UNCHECKED_CAST")
    internal val apiClazz: Class<out Module<*, *>> by lazy {
        _apiClazz ?: this.javaClass.superclass as Class<out Module<*, *>>
    }
    internal val apiClazzName: String
        get() = apiClazz.simpleName
    internal val internalModuleInit: MODULE_INIT
        get() = init
    internal val moduleUnit: ModuleUnit by lazy(LazyThreadSafetyMode.NONE) {
        val moduleUnit by ModuleUnit.Delegate(this.javaClass, this)
        moduleUnit
    }

    protected fun dependOn(moduleClazz: Class<out Module<*, *>>, implClazzName: String) {
        moduleUnit.dependOn(moduleClazz, implClazzName)
    }
}