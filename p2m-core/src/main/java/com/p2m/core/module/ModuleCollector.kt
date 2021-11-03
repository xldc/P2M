package com.p2m.core.module

import com.p2m.core.internal.module.ModuleRegister

abstract class ModuleCollector {
    private val moduleImplClass = mutableSetOf<String>()

    protected fun collect(implClassStr: String) {
        moduleImplClass.add(implClassStr)
    }

    internal fun inject(moduleRegister: ModuleRegister<*>) {
        for (implClassStr in moduleImplClass) {
            @Suppress("UNCHECKED_CAST")
            try {
                val implClass = Class.forName(implClassStr) as Class<out Module<*>>
                moduleRegister.register(implClass)
            }catch (e: NoClassDefFoundError) {
                val moduleName = implClassStr.substringAfter("_", "")
                if (moduleName.isNotEmpty()) {
                    throw RuntimeException("$moduleName does not exist, please check your config in settings.gradle.", e)
                }else {
                    throw e
                }
            }
        }
    }
}