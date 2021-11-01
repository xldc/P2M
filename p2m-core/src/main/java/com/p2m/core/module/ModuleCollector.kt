package com.p2m.core.module

import com.p2m.core.internal.module.ModuleRegister

abstract class ModuleCollector {
    private val moduleImplClazz = mutableSetOf<String>()

    protected fun collect(implClazzStr: String) {
        moduleImplClazz.add(implClazzStr)
    }

    internal fun inject(moduleRegister: ModuleRegister<*>) {
        for (implClazzStr in moduleImplClazz) {
            @Suppress("UNCHECKED_CAST")
            try {
                val implClazz = Class.forName(implClazzStr) as Class<out Module<*, *>>
                moduleRegister.register(implClazz)
            }catch (e: NoClassDefFoundError) {
                val moduleName = implClazzStr.substringAfter("_", "")
                if (moduleName.isNotEmpty()) {
                    throw RuntimeException("$moduleName does not exist, please check your config in settings.gradle.", e)
                }else {
                    throw e
                }
            }
        }
    }
}