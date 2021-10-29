package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleFactory

internal class DefaultModuleFactory : ModuleFactory {
    override fun <MODULE : Module<*, *>> newInstance(clazz: Class<MODULE>): MODULE {
        return clazz.newInstance()
    }
}