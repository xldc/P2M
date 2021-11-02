package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleUnit

internal class ModuleUnitImpl constructor(
    override val moduleImplClazz: Class<out Module<*>>,
    val module: Module<*>
) : ModuleUnit {
    override val dependencies = hashSetOf<Class<out Module<*>>>()

    override fun dependOn(clazz: Class<out Module<*>>, implClazzName: String) {
        @Suppress("UNCHECKED_CAST")
        val implClazz = Class.forName(implClazzName) as Class<out Module<*>>
        dependencies.add(implClazz)
    }
}