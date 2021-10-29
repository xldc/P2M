package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleUnit

/**
 * Module register.
 */
interface ModuleRegister<UNIT : ModuleUnit> {

    /**
     * Register a module.
     *
     * @param implClazz the module class.
     */
    fun register(implClazz: Class<out Module<*, *>>): UNIT

}