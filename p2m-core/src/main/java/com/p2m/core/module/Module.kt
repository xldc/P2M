package com.p2m.core.module

import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.Task


/**
 * A module has one [Module] only.
 *
 * Module initialization has three stages, in the following order:
 *  * evaluate, corresponds to [Module.onEvaluate].
 *  * execute,  corresponds to [Task.onExecute].
 *  * executed, corresponds to [Module.onExecuted].
 *
 * The module initialization has the following formula:
 *  * Within a module, the execution order must be
 *  [Module.onEvaluate] > [Task.onExecute] > [Module.onExecuted].
 *  * If module A depends on module B, the execution order must be
 *  [Module.onExecuted] of module B > [Module.onExecuted] of module A.
 *  * If module A depends on module B and B depends on C, the execution order must be
 *  [Module.onExecuted] of module C > [Module.onExecuted] of module A.
 *
 * Example, has a Main module and a Account module, Main use Login, so it depend on Account module:
 *
 * ```
 * @Event
 * interface AccountEvent : ModuleEvent {
 *      @EventField
 *      val loginState: Boolean
 * }
 *
 * class LoadLoginStateTask : Task<UserDiskCache, Boolean>() {
 *      // All dependant task complete executed already, can get they output here.
 *      // All dependant module has been initialized, can get they module api here.
 *      // running in work thread.
 *      override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
 *          val userCache = input
 *          val loginSuccess = userCache.readLoginSuccess()
 *          output = loginSuccess
 *      }
 * }
 *
 * @ModuleInitializer
 * class AccountModule : Module {
 *      // Register some task to complete necessary initialization.
 *      // running in work thread.
 *      override fun onEvaluate(taskRegister: TaskRegister) {
 *          // register a task of LoadLoginStateTask for read login state from disk cache.
 *          taskRegister.register(LoadLoginStateTask::class.java, instance of UserDiskCache)
 *      }
 *
 *      // All task of Account module complete executed already, can get they output here.
 *      // All dependant module has been initialized, can get they module api here.
 *      // running in main thread.
 *      override fun onExecuted(
 *          taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider
 *      ) {
 *
 *          // get login state.
 *          val loginSuccess = taskOutputProvider.getOutputOf(LoadLoginStateTask::class.java)
 *
 *          // save login state to event holder.
 *          moduleProvider
 *              .moduleOf(Account::class.java)
 *              .event
 *              .loginState // you can set value, get value and observe by it.
 *              .setValue(loginSuccess)
 *      }
 * }
 *
 * @ModuleInitializer
 * class MainModule : Module {
 *      // Register some task to complete necessary initialization.
 *      // running in work thread.
 *      override fun onEvaluate(taskRegister: TaskRegister) {
 *          // register some task...
 *      }
 *
 *      // All task of Main module complete executed already, can get they output here.
 *      // All dependant module has been initialized, can get they module api here.
 *      // running in main thread.
 *      override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
 *          // Account module has been initialized, get the module api.
 *          val accountModuleApi = moduleProvider.moduleOf(Account::class.java)
 *
 *          // observe loginState of Account.
 *          accountModuleApi.event.loginState.observeForeverNoLoss { loginState ->
 *              // doing
 *          }
 *      }
 * }
 * ```
 *
 * Module begin initialization by call `P2M.driverBuilder().build().open()`
 * in your custom application.
 *
 * see more: https://github.com/wangdaqi77/P2M
 *
 * @see onEvaluate onEvaluate - evaluate stage.
 * @see onExecuted onExecuted - executed stage.
 * @see TaskRegister TaskRegister - register some task.
 * @see Task Task - is the smallest unit in a module to perform initialization.
 * @see TaskOutputProvider TaskOutputProvider - get some task output.
 * @see SafeModuleProvider SafeModuleProvider - get some module api.
 *
 */
interface Module : OnEvaluateListener, OnExecutedListener

interface OnEvaluateListener{
    /**
     * Evaluate stage of itself.
     *
     * Here, you can use [TaskRegister] to register some task for help initialize module
     * fast, and then these tasks will be executed in the order of dependencies.
     *
     * Note, it running in work thread.
     *
     * @param taskRegister task register.
     *
     * @see TaskRegister TaskRegister - register some task.
     * @see Task Task - is the smallest unit in a module to perform initialization.
     */
    fun onEvaluate(taskRegister: TaskRegister)
}

interface OnExecutedListener{
    /**
     * Executed stage of itself, indicates will completed initialized of the module.
     *
     * Called when its all tasks be completed and all dependencies completed initialized.
     *
     * Here, you can use [TaskOutputProvider] to get some output of itself tasks,
     * also use [SafeModuleProvider] to get some dependency module api.
     *
     * Note, it running in main thread.
     *
     * @param taskOutputProvider task output provider.
     * @param moduleProvider module provider.
     *
     * @see TaskOutputProvider TaskOutputProvider - get some task output.
     * @see SafeModuleProvider SafeModuleProvider - get some module.
     */
    fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider)
}

class EmptyModule : Module {

    override fun onEvaluate(taskRegister: TaskRegister) { }

    override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) { }
}
