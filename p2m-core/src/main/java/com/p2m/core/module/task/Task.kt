package com.p2m.core.module.task

import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.Module

/**
 * A task is the smallest unit in a module to perform initialization.
 *
 * Only recommended to execute lightweight work, because [Task] is to complete the
 * module initialization design.
 *
 * @param INPUT set [input] when register a task.
 * @param OUTPUT set [output] when completed work, so should set it up in the [onExecute].
 *
 * @see Module - How to register a task? and how to use a task?
 */
abstract class Task<INPUT, OUTPUT> {

    internal var inputObj: Any? = null

    @Suppress("UNCHECKED_CAST")
    protected val input: INPUT?
        get() = inputObj as? INPUT

    @JvmField
    var output: OUTPUT? = null

    /**
     *
     * The task executing, called after [Module.onEvaluate] and before [Module.onExecuted].
     *
     * NOTE: Running in work thread.
     *
     * You can use [taskOutputProvider] get some dependency task output, also can use
     * [moduleProvider] get some dependency module.
     *
     * @param taskOutputProvider task output provider
     * @param moduleProvider module provider
     *
     * @see TaskOutputProvider TaskOutputProvider - get some task output.
     * @see SafeModuleProvider SafeModuleProvider - get some module api.
     */
     abstract fun onExecute(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider)
}