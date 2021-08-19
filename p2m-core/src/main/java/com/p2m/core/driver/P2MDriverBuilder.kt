package com.p2m.core.driver

import com.p2m.core.module.OnEvaluateListener
import com.p2m.core.module.OnExecutedListener
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister


interface P2MDriverBuilder {

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    fun onEvaluate(block: (taskRegister: TaskRegister) -> Unit): P2MDriverBuilder

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    fun onEvaluate(evaluateListener: OnEvaluateListener): P2MDriverBuilder

    /**
     * Calling when all depend module be completed.
     * Can use depend module here.
     */
    fun onExecuted(block: (taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) -> Unit): P2MDriverBuilder

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