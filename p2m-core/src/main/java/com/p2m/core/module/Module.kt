package com.p2m.core.module

import com.p2m.core.internal.moduleName
import com.p2m.core.internal.module.ModuleUnitImpl
import com.p2m.core.internal.module.ModuleVisitor

/**
 * Each module has one and only one [Module] on runtime, it has a `init` for
 * its own initialization, and has a `api` is exposed for its own and external module.
 *
 * Public sub class of each module is auto generated by P2M-APT,  it's class name
 * is defined module name in `settings.gradle`.
 *
 * External module can access `api` when its own `init` initialization be completed,
 *
 * Call `P2M.apiOf(public module class of dependency)` to get instance of `api`.
 *
 * For example, `Main` module use `Account` module, so `Main` depends on `Account`,
 * in `settings.gradle`:
 * ```
 * p2m {
 *      module("Account") {
 *          // some configuration
 *      }
 *
 *      module("Main") {
 *          dependencies {
 *              module("Account")
 *          }
 *      }
 * }
 * ```
 * Get instance of `api` of `Account` in `Main`:
 * ```
 * val accountApi = P2M.apiOf(Account)
 * val accountLauncher = accountApi.launcher
 * val accountService = accountApi.service
 * val accountEvent = accountApi.event
 * ```
 *
 * see more at [doc](https://github.com/wangdaqi77/P2M)
 *
 * @see ModuleApi  - a module api.
 * @see ModuleInit - a module initialization.
 */
abstract class Module<MODULE_API : ModuleApi<*, *, *>> {
    abstract val api: MODULE_API
    protected abstract val init: ModuleInit
    @Suppress("UNCHECKED_CAST")
    protected open val publicClass: Class<out Module<*>> = this.javaClass.superclass as Class<out Module<*>>
    internal val dependencies = hashSetOf<String>()
    internal val internalInit: ModuleInit
        get() = init
    internal val internalModuleUnit by lazy(LazyThreadSafetyMode.NONE) {
        ModuleUnitImpl(this, this.javaClass, publicClass)
    }

    internal fun accept(visitor: ModuleVisitor){
        visitor.visit(this)
    }

    protected fun dependOn(moduleName: String) {
        dependencies.add(moduleName)
    }

    override fun toString(): String {
        return publicClass.moduleName
    }
}