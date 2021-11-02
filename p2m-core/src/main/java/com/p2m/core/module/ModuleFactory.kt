package com.p2m.core.module

interface ModuleFactory {
    fun <MODULE : Module<*>> newInstance(clazz: Class<MODULE>): MODULE
}