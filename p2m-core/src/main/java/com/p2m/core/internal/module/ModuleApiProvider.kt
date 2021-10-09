package com.p2m.core.internal.module

import android.content.Context
import com.p2m.core.P2M
import com.p2m.core.internal.log.logI
import com.p2m.core.module.EmptyModuleApi
import com.p2m.core.module.ModuleApi
import com.p2m.core.module.ModuleProvider
import com.p2m.core.module.SafeModuleProvider

internal interface ModuleRegister {
    fun registerModule(apiClass: Class<out ModuleApi<*, *, *>>, apiImpl: ModuleApi<*, *, *>)
}

internal class InnerModuleManager(provider: ModuleProvider, register: ModuleRegister) :
    ModuleProvider by provider,
    ModuleRegister by register

internal object DefaultModuleManager : ModuleProvider, ModuleRegister {
    private val apis: HashMap<Class<out ModuleApi<*, *, *>>, ModuleApi<*, *, *>?> = hashMapOf()

    override fun registerModule(apiClass: Class<out ModuleApi<*, *, *>>, apiImpl: ModuleApi<*, *, *>) {
        check(apiClass.isInstance(apiImpl)) {
            "Not match. Check ${apiClass.canonicalName}, ${apiImpl::class.java.canonicalName}"
        }
        if (apis[apiClass] != null) {
            if (apiClass == EmptyModuleApi::class.java) {
                logI("${apiClass.canonicalName} registered already.")
                return
            }
            throw IllegalStateException("${apiClass.canonicalName} registered already.")
        } else {

            apis[apiClass] = apiImpl // register in  main thread
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun <MODULE_API : ModuleApi<*, *, *>> moduleApiOf(clazz: Class<MODULE_API>): MODULE_API {
        return apis[clazz] as MODULE_API
    }

}

internal class SafeModuleProviderImpl constructor(override val context: Context, private val moduleName: String, private val selfApi: ModuleApi<*, *, *>?) :
    SafeModuleProvider,
    ModuleProvider {

    private fun <MODULE_API : ModuleApi<*, *, *>> checkSelf(clazz: Class<MODULE_API>): Boolean {
        return clazz.isInstance(selfApi)
    }

    private fun <MODULE_API : ModuleApi<*, *, *>> getSelf(clazz: Class<MODULE_API>): MODULE_API {
        check (checkSelf(clazz)) { "Please Call get(${moduleName}::class)." }
        @Suppress("UNCHECKED_CAST")
        return selfApi as MODULE_API
    }

    override fun<MODULE_API : ModuleApi<*, *, *>> moduleApiOf(clazz: Class<MODULE_API>): MODULE_API{
        if (checkSelf(clazz)) {
            return getSelf(clazz)
        }
        return DefaultModuleManager.moduleApiOf(clazz)
    }
}