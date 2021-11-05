package com.p2m.core.module.task

import android.content.Context
import com.p2m.core.module.ModuleInit

/**
 * A task is the smallest unit in a module to perform necessary initialization.
 *
 * It is design for the module complete necessary initialization.
 *
 * Note:
 *  * Only recommended to execute lightweight work.
 *  * The output must be set synchronously during [onExecute], so as to ensure that
 *  the dependency can obtain the output safely.
 *
 * @param INPUT corresponds type of [input].
 * @param OUTPUT corresponds type of [output].
 *
 * @see ModuleInit - How to register a task and how to use a task.
 */
abstract class Task<INPUT, OUTPUT> {

    internal var inputObj: Any? = null

    @Suppress("UNCHECKED_CAST")
    protected val input: INPUT?
        get() = inputObj as? INPUT

    var output: OUTPUT? = null

    /**
     * The task executing, called after [ModuleInit.onEvaluate] and before [ModuleInit.onExecuted].
     *
     * Note:
     *  * Running in work thread.
     *
     * @param taskOutputProvider task output provider, can get task output of some dependency.
     *
     * @see TaskOutputProvider TaskOutputProvider - get some task output.
     */
     abstract fun onExecute(context: Context, taskOutputProvider: TaskOutputProvider)
}