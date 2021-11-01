package com.p2m.core.internal.module

import com.p2m.core.module.ModuleCollector
import com.p2m.core.module.ModuleCollectorFactory

internal class DefaultModuleCollectorFactory : ModuleCollectorFactory {
    override fun newInstance(clazzName: String): ModuleCollector {
        return Class.forName(clazzName).newInstance() as ModuleCollector
    }
}