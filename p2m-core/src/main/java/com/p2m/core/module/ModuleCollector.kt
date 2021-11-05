package com.p2m.core.module

import com.p2m.core.app.App
import com.p2m.core.internal.module.ModuleVisitor

abstract class ModuleCollector {
    private val moduleImplClass = mutableSetOf<String>()

    protected fun collect(implClassStr: String) {
        moduleImplClass.add(implClassStr)
    }

    internal fun injectFrom(
        app: App,
        moduleFactory: ModuleFactory,
        moduleVisitor: ModuleVisitor,
        block: (Module<*>) -> Unit
    ) {
        injectModule(app, moduleVisitor)
        inject(moduleFactory, moduleVisitor, block)
    }

    private fun inject(
        moduleFactory: ModuleFactory,
        moduleVisitor: ModuleVisitor,
        block: (Module<*>) -> Unit
    ) {
        for (implClassStr in moduleImplClass) {
            @Suppress("UNCHECKED_CAST")
            try {
                val implClass = Class.forName(implClassStr) as Class<out Module<*>>
                injectModule(moduleFactory, moduleVisitor, implClass, block)
            }catch (e: NoClassDefFoundError) {
                val moduleName = implClassStr.substringAfter("_", "")
                if (moduleName.isNotEmpty()) {
                    throw RuntimeException("$moduleName does not exist, please check your config in settings.gradle.", e)
                }else {
                    throw e
                }
            }
        }
    }

    private fun injectModule(module: Module<*>, moduleVisitor: ModuleVisitor){
        module.accept(moduleVisitor)
    }

    private fun injectModule(
        moduleFactory: ModuleFactory,
        moduleVisitor: ModuleVisitor,
        implClass: Class<out Module<*>>,
        block: (Module<*>) -> Unit
    ) {
        val module = moduleFactory.newInstance(implClass)
        injectModule(module, moduleVisitor)
        block.invoke(module)
        for (dependency in module.internalModuleUnit.getDependencies()) {
            injectModule(moduleFactory, moduleVisitor, dependency, block)
        }
    }
}