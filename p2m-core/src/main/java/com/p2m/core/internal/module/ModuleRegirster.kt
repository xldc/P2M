package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleUnit

/**
 * Module register.
 */
internal interface ModuleRegister<UNIT : ModuleUnit> {

    /**
     * Register a module.
     *
     * @param implClass the module class.
     */
    fun register(implClass: Class<out Module<*>>): UNIT

}