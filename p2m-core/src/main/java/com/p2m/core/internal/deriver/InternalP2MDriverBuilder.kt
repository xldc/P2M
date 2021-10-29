package com.p2m.core.internal.deriver

import android.app.Application
import android.content.Context
import com.p2m.core.driver.P2MDriver
import com.p2m.core.driver.P2MDriverBuilder
import com.p2m.core.driver.P2MDriverState
import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.module.OnEvaluateListener
import com.p2m.core.module.OnExecutedListener
import com.p2m.core.module.SafeModuleApiProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister
import com.p2m.core.module.task.TaskUnit

internal class InternalP2MDriverBuilder(
    val context: Application,
    val moduleContainer: ModuleContainerImpl,
    val driverState: P2MDriverState
): P2MDriverBuilder {

    internal var executeListener: OnExecutedListener? = null
    internal var evaluateListener: OnEvaluateListener? = null

    /**
     * Calling when evaluate for self.
     * Can init self here.
     */
    override fun onEvaluate(block: (context: Context, taskRegister: TaskRegister<out TaskUnit>) -> Unit): P2MDriverBuilder {
        onEvaluate(object : OnEvaluateListener {
            override fun onEvaluate(context: Context, taskRegister: TaskRegister<out TaskUnit>) {
                block(context, taskRegister)
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
    override fun onExecuted(block: (context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) -> Unit): P2MDriverBuilder {
        onExecuted(object : OnExecutedListener {
            override fun onExecuted(context: Context, taskOutputProvider: TaskOutputProvider, moduleApiProvider: SafeModuleApiProvider) {
                block(context, taskOutputProvider, moduleApiProvider)
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
        return InternalP2MDriver(this)
    }

}
