package com.p2m.core.module

import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.internal.module.ManifestModuleFinder

abstract class ModuleCollector {
    private val modules = mutableSetOf<String>()

    protected fun collect(moduleName: String) {
        modules.add(moduleName)
    }

    internal fun injectForExternal(
        manifestModuleFinder: ManifestModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl,
        vararg moduleImplClass: String
    ) {
        for (implClass in moduleImplClass) {
            injectModule(manifestModuleFinder, moduleFactory, moduleContainer, implClass)
        }
    }

    internal fun injectForAllFromTop(
        topModule: Module<*>,
        manifestModuleFinder: ManifestModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl
    ) {
        for (moduleName in modules) {
            injectModule(manifestModuleFinder, moduleFactory, moduleContainer, moduleName).also {
                topModule.internalModuleUnit.dependOn(it.internalModuleUnit.moduleImplClass)
            }
        }
    }

    internal fun injectForCreatedModule(module: Module<*>, moduleContainer: ModuleContainerImpl){
        module.accept(moduleContainer)
    }

    private fun injectModule(
        manifestModuleFinder: ManifestModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl,
        moduleName: String
    ): Module<*> {
        val implClass = manifestModuleFinder.getModuleImplClass(moduleName)
        return injectModule(manifestModuleFinder, moduleFactory, moduleContainer, implClass)
    }

    private fun injectModule(
        manifestModuleFinder: ManifestModuleFinder,
        moduleFactory: ModuleFactory,
        moduleContainer: ModuleContainerImpl,
        implClass: Class<out Module<*>>
    ): Module<*> {
        val module = moduleContainer.find(implClass) ?: moduleFactory.newInstance(implClass).also {
            injectForCreatedModule(it, moduleContainer)
        }
        for (moduleNameOfDependency in module.dependencies) {
            injectModule(manifestModuleFinder, moduleFactory, moduleContainer, moduleNameOfDependency).also {
                module.internalModuleUnit.dependOn(it.internalModuleUnit.moduleImplClass)
            }
        }
        return module
    }
}