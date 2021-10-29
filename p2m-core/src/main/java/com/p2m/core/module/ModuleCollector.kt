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
            val implClazz = Class.forName(implClazzStr) as Class<out Module<*, *>>
            moduleRegister.register(implClazz)
        }
    }
}