package com.p2m.core.internal.module

import com.p2m.core.module.Module
import com.p2m.core.module.ModuleFactory
import com.p2m.core.module.ModuleUnit

internal interface ModuleContainer<UNIT : ModuleUnit>{
    /**
     * Found a task in the container.
     *
     * @param clazz  apiClazz or implClazz.
     */
    fun find(clazz: Class<out Module<*, *>>): UNIT?

    fun getAll(): Collection<UNIT>
}


internal class ModuleContainerImpl(
    val topModuleImplClazz: Class<out Module<*, *>>,
    private val moduleFactory: ModuleFactory
) : ModuleRegister<ModuleUnitImpl>, ModuleContainer<ModuleUnitImpl> {
    // K:impl V:ModuleUnitImpl
    private val container = HashMap<Class<out Module<*, *>>, ModuleUnitImpl>()
    // K:api V:impl
    private val apiImplMap = HashMap<Class<out Module<*, *>>, Class<out Module<*, *>>>()
    init {
        register(topModuleImplClazz)
    }

    /**
     * 根据依赖关系递归注册。
     */
    override fun register(implClazz: Class<out Module<*, *>>): ModuleUnitImpl {
        if (container.containsKey(implClazz)) return container[implClazz]!!
        val module = moduleFactory.newInstance(implClazz)
        val moduleUnit = module.moduleUnit as ModuleUnitImpl
        apiImplMap[module.apiClazz] = implClazz
        container[implClazz] = moduleUnit
        moduleUnit.dependencies.forEach{ register(it) }
        return moduleUnit
    }

    override fun find(clazz: Class<out Module<*, *>>): ModuleUnitImpl? = container[apiImplMap[clazz]] ?: container[clazz]

    override fun getAll(): Collection<ModuleUnitImpl> = container.values
}