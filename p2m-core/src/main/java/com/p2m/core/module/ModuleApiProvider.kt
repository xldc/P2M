package com.p2m.core.module

interface ModuleApiProvider {
    /**
     * Get a module api of [clazz].
     *
     * @param clazz its type name is defined module name in settings.gradle.
     */
    fun
            <MODULE_API : ModuleApi<*, *, *>, MODULE : Module<MODULE_API, *>>
            moduleApiOf(clazz: Class<MODULE>): MODULE_API
}

interface SafeModuleApiProvider : ModuleApiProvider