package com.p2m.core.module

import com.p2m.core.internal.module.ModuleVisitor

abstract class ModuleCollector {
    private val moduleImplClass = mutableSetOf<String>()

    protected fun collect(implClazz: String) {
        moduleImplClass.add(implClazz)
    }

    internal fun collectExternal(vararg implClazz: String) {
        moduleImplClass.addAll(implClazz.asList())
    }

    internal fun injectForAllUncreatedModule(
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

    internal fun injectForCreatedModule(module: Module<*>, moduleVisitor: ModuleVisitor){
        module.accept(moduleVisitor)
    }

    private fun injectModule(
        moduleFactory: ModuleFactory,
        moduleVisitor: ModuleVisitor,
        implClass: Class<out Module<*>>,
        block: (Module<*>) -> Unit
    ) {
        val module = moduleFactory.newInstance(implClass)
        injectForCreatedModule(module, moduleVisitor)
        block.invoke(module)
        for (dependency in module.internalModuleUnit.getDependencies()) {
            injectModule(moduleFactory, moduleVisitor, dependency, block)
        }
    }
}