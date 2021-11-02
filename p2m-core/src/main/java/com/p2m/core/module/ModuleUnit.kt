package com.p2m.core.module

import com.p2m.core.internal.module.ModuleUnitImpl
import kotlin.reflect.KProperty

/**
 * The unit of module.
 */
interface ModuleUnit {
    class Delegate(moduleImplClazz: Class<out Module<*>>, module: Module<*>) {
        private val real by lazy { ModuleUnitImpl(moduleImplClazz, module) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ModuleUnit = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ModuleUnit) = Unit
    }

    val moduleImplClazz: Class<out Module<*>>

    val dependencies: Set<Class<out Module<*>>>

    /**
     * Adds the given dependencies to this module.
     */
    fun dependOn(clazz: Class<out Module<*>>, implClazzName: String)
}