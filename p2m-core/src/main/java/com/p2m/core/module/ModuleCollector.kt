package com.p2m.core.module

import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.internal.module.ModuleFinder

abstract class ModuleCollector {
    private val modules = mutableSetOf<String>()

    protected fun collect(moduleName: String) {
        modules.add(moduleName)
    }

    internal fun collectExternal(vararg moduleName: String) {
        modules.addAll(moduleName.asList())
    }

    internal fun injectForAllFromTop(
        topModule: Module<*>,
        moduleFinder: ModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl
    ) {
        for (moduleName in modules) {
            injectModule(moduleFinder, moduleFactory, moduleContainer, moduleName).also {
                topModule.internalModuleUnit.dependOn(it.internalModuleUnit.moduleImplClass)
            }
        }
    }

    internal fun injectForCreatedModule(module: Module<*>, moduleContainer: ModuleContainerImpl){
        module.accept(moduleContainer)
    }

    private fun injectModule(
        moduleFinder: ModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl,
        moduleName: String
    ): Module<*> {
        val implClass = moduleFinder.getModuleImplClass(moduleName)
        val module = moduleContainer.find(implClass) ?: moduleFactory.newInstance(implClass).also {
            injectForCreatedModule(it, moduleContainer)
        }
        for (moduleNameOfDependency in module.dependencies) {
            injectModule(moduleFinder, moduleFactory, moduleContainer, moduleNameOfDependency).also {
                module.internalModuleUnit.dependOn(it.internalModuleUnit.moduleImplClass)
            }
        }
        return module
    }
}