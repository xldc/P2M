package com.p2m.core.driver

import android.content.Context
import com.p2m.core.module.OnEvaluateListener
import com.p2m.core.module.OnExecutedListener
import com.p2m.core.module.SafeModuleApiProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.TaskUnit


interface P2MDriverBuilder {

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    fun onEvaluate(block: (context: Context, taskRegister: TaskRegister<out TaskUnit>) -> Unit): P2MDriverBuilder

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    fun onEvaluate(evaluateListener: OnEvaluateListener): P2MDriverBuilder

    /**
     * Calling when all depend module be completed.
     * Can use depend module here.
     */
    fun onExecuted(block: (context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) -> Unit): P2MDriverBuilder

    /**
     * Calling when all depend module be completed.
     * Can use depend module here.
     */
    fun onExecuted(executeListener: OnExecutedListener): P2MDriverBuilder

    /**
     * To build [P2MDriver].
     */
    fun build(): P2MDriver
}