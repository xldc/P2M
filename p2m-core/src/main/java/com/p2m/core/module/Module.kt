package com.p2m.core.module

import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.Task


/**
 * A module project has one [Module] only.
 *
 * A [Module] has two stages: evaluate and executed.
 *
 * The evaluate stage corresponds to [onEvaluate] and the executed stage corresponds to
 * [onExecuted].
 *
 * Example, has a Main module and a Login module, Main use Login, so it depend on Login module:
 *
 * ```
 * class LoadLoginStatusTask : Task<SharedPreferences, Boolean>() {
 *      // running in work thread.
 *      override fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
 *          val sp = input
 *          val loginSuccess = sp.getBoolean("login_success", false)
 *          output = loginSuccess
 *      }
 * }
 *
 * @Event
 * interface LoginEvent : ModuleEvent {
 *      @EventField
 *      val loginStatus: Boolean
 * }
 *
 * @ModuleInitializer
 * class LoginModule : Module {
 *
 *      override fun onEvaluate(taskRegister: TaskRegister) {
 *          // register a task of LoadLoginStatusTask for login status.
 *          taskRegister.register(LoadLoginStatusTask::class.java, instance of SharedPreferences)
 *      }
 *
 *      override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
 *          // LoadLoginStatusTask complete executed already.
 *          // get login status.
 *          val loginSuccess = taskOutputProvider.getOutputOf(LoadLoginStatusTask::class.java)
 *
 *          // save login status.
 *          moduleProvider.moduleOf(Login::class.java).event.loginStatus.setValue(loginSuccess)
 *      }
 * }
 *
 * @ModuleInitializer
 * class MainModule : Module {
 *
 *      override fun onEvaluate(taskRegister: TaskRegister) {
 *          // register some task...
 *      }
 *
 *      override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
 *          // Login module has been initialized, get the module.
 *          val loginModule = provider.moduleOf(Login::class.java)
 *
 *          // observe a event for loginStatus.
 *          loginModule.event.loginStatus.observeForever { loginStatus ->
 *              // doing
 *          }
 *      }
 * }
 * ```
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
     * Here, you can use [taskRegister] to register some task for help initialize module
     * fast, and then these tasks will be executed order.
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
     * Here, you can use [taskOutputProvider] to get some output of itself tasks,
     * also use [moduleProvider] to get some dependency module.
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
