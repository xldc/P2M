package com.p2m.core.internal.deriver

import android.content.Context
import com.p2m.core.driver.P2MDriver
import com.p2m.core.driver.P2MDriverBuilder
import com.p2m.core.internal.module.InnerModuleManager
import com.p2m.core.module.OnEvaluateListener
import com.p2m.core.module.OnExecutedListener
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

internal class InternalP2MDriverBuilder constructor(internal val context: Context, internal val innerModuleManager: InnerModuleManager): P2MDriverBuilder {

    internal var executeListener: OnExecutedListener? = null
    internal var evaluateListener: OnEvaluateListener? = null

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    override fun onEvaluate(block: (taskRegister: TaskRegister) -> Unit): P2MDriverBuilder {
        onEvaluate(object : OnEvaluateListener {
            override fun onEvaluate(taskRegister: TaskRegister) {
                block(taskRegister)
            }
        })
        return this
    }

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    override fun onEvaluate(evaluateListener: OnEvaluateListener): P2MDriverBuilder {
        check(this.evaluateListener == null) { "Only one onEvaluate can be set." }
        this.evaluateListener = evaluateListener
        return this
    }

    /**
     * Calling when all depend module be completed.
     * Can use depend module here.
     */
    override fun onExecuted(block: (taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) -> Unit): P2MDriverBuilder {
        onExecuted(object : OnExecutedListener {
            override fun onExecuted(
                taskOutputProvider: TaskOutputProvider,
                moduleProvider: SafeModuleProvider
            ) {
                block(taskOutputProvider, moduleProvider)
            }
        })
        return this
    }

    /**
     * Calling when all depend module be completed.
     * Can use depend module here.
     */
    override fun onExecuted(executeListener: OnExecutedListener): P2MDriverBuilder {
        check(this.executeListener == null) { "Only one onExecuted can be set." }
        this.executeListener = executeListener
        return this
    }

    override fun build(): P2MDriver {
        InternalP2MDriver.builder = this
        return InternalP2MDriver
    }

}
