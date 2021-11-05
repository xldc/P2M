package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleFactory
import com.p2m.core.module.ModuleUnit

internal interface ModuleContainer{
    /**
     * Found a module in the container.
     *
     * @param clazz  apiClass or implClass.
     */
    fun find(clazz: Class<out Module<*>>): Module<*>?

    fun getAll(): Collection<Module<*>>
}


internal class ModuleContainerImpl : ModuleRegister, ModuleContainer, ModuleVisitor {
    // K:impl V:ModuleUnitImpl
    private val container = HashMap<Class<out Module<*>>, Module<*>>()
    // K:public api V:impl
    private val clazzMap = HashMap<Class<out Module<*>>, Class<out Module<*>>>()

    override fun register(module: Module<*>) {
        val moduleUnit = module.internalModuleUnit
        if (container.containsKey(moduleUnit.moduleImplClass)) return
        clazzMap[moduleUnit.modulePublicClass] = moduleUnit.moduleImplClass
        container[moduleUnit.moduleImplClass] = module
    }

    override fun visit(module: Module<*>) {
        register(module)
    }

    override fun find(clazz: Class<out Module<*>>): Module<*>? = container[clazzMap[clazz]] ?: container[clazz]

    override fun getAll(): Collection<Module<*>> = container.values
}