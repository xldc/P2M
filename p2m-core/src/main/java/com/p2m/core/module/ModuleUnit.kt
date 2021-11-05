package com.p2m.core.module

/**
 * The unit of module.
 */
interface ModuleUnit {

    val moduleName : String
        get() = modulePublicClass.simpleName

    val moduleImplClass: Class<out Module<*>>

    val modulePublicClass: Class<out Module<*>>

    /**
     * Adds the given dependencies to this module.
     */
    fun dependOn(publicClass: Class<out Module<*>>, implClassName: String)

    fun dependOn(publicClass: Class<out Module<*>>, implClass: Class<out Module<*>>)

    fun getDependencies(): Set<Class<out Module<*>>>
}