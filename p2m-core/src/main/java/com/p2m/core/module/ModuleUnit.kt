package com.p2m.core.module

import com.p2m.core.internal.module.ModuleUnitImpl
import kotlin.reflect.KProperty

/**
 * The unit of module.
 */
interface ModuleUnit {
    class Delegate(
        module: Module<*>,
        moduleImplClass: Class<out Module<*>>,
        modulePublicClass: Class<out Module<*>> ) {
        private val real by lazy { ModuleUnitImpl(module, moduleImplClass, modulePublicClass) }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ModuleUnit = real

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ModuleUnit) = Unit
    }

    val moduleName : String
        get() = modulePublicClass.simpleName

    val moduleImplClass: Class<out Module<*>>

    val modulePublicClass: Class<out Module<*>>

    /**
     * Adds the given dependencies to this module.
     */
    fun dependOn(clazz: Class<out Module<*>>, implClassName: String)

    fun getDependencies(): Set<Class<out Module<*>>>
}