package com.p2m.core.module

import android.content.Context

interface ModuleProvider{
    /**
     * @param clazz its type name is defined module name in settings.gradle.
     */
    fun<MODULE_API : ModuleApi<*, *, *>> moduleOf(clazz: Class<MODULE_API>): MODULE_API
}

interface SafeModuleProvider : ModuleProvider {
    val context: Context
}