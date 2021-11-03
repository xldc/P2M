package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleFactory
import com.p2m.core.module.ModuleUnit

internal interface ModuleContainer<UNIT : ModuleUnit>{
    /**
     * Found a task in the container.
     *
     * @param clazz  apiClass or implClass.
     */
    fun find(clazz: Class<out Module<*>>): UNIT?

    fun getAll(): Collection<UNIT>
}


internal class ModuleContainerImpl(
    val topModuleImplClass: Class<out Module<*>>,
    private val moduleFactory: ModuleFactory
) : ModuleRegister<ModuleUnitImpl>, ModuleContainer<ModuleUnitImpl> {
    // K:impl V:ModuleUnitImpl
    private val container = HashMap<Class<out Module<*>>, ModuleUnitImpl>()
    // K:public api V:impl
    private val clazzMap = HashMap<Class<out Module<*>>, Class<out Module<*>>>()
    init {
        register(topModuleImplClass)
    }

    override fun register(implClass: Class<out Module<*>>): ModuleUnitImpl {
        if (container.containsKey(implClass)) return container[implClass]!!
        val module = moduleFactory.newInstance(implClass)
        val moduleUnit = module.internalModuleUnit as ModuleUnitImpl
        clazzMap[moduleUnit.modulePublicClass] = implClass
        container[implClass] = moduleUnit
        moduleUnit.getDependencies().forEach{ register(it) }
        return moduleUnit
    }

    override fun find(clazz: Class<out Module<*>>): ModuleUnitImpl? = container[clazzMap[clazz]] ?: container[clazz]

    override fun getAll(): Collection<ModuleUnitImpl> = container.values
}