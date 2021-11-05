package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleUnit

/**
 * Module register.
 */
internal interface ModuleRegister {

    /**
     * Register a module.
     *
     * @param module a module.
     */
    fun register(module: Module<*>)
}