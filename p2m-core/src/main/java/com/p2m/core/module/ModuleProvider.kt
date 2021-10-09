package com.p2m.core.module

import android.content.Context

interface ModuleProvider{
    /**
     * Get a module api of [clazz].
     *
     * @param clazz its type name is defined module name in settings.gradle.
     */
    fun<MODULE_API : ModuleApi<*, *, *>> moduleApiOf(clazz: Class<MODULE_API>): MODULE_API
}

interface SafeModuleProvider : ModuleProvider {
    val context: Context
}