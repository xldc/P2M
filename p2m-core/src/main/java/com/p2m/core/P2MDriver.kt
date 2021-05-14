package com.p2m.core

import android.content.Context
import androidx.annotation.MainThread
import com.p2m.core.internal.InternalP2MDriver
import com.p2m.core.internal.module.InnerModuleManager
import com.p2m.core.module.OnEvaluateListener
import com.p2m.core.module.OnExecutedListener
import com.p2m.core.module.SafeModuleProvider
import com.p2m.core.module.task.TaskOutputProvider
import com.p2m.core.module.task.TaskRegister

/**
 * P2M Driver.
 */
interface P2MDriver {

    /**
     * Open drive.
     * Main thread will be blocked.
     */
    @MainThread
    fun open()

    class Builder internal constructor(internal val context: Context, internal val innerModuleManager: InnerModuleManager) {

        internal var executeListener: OnExecutedListener? = null
        internal var evaluateListener: OnEvaluateListener? = null

        /**
         * Calling when evaluate for self.
         * Can init self here.
         */
        fun onEvaluate(block: (taskRegister: TaskRegister) -> Unit): P2MDriver.Builder {
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
        fun onEvaluate(evaluateListener: OnEvaluateListener): P2MDriver.Builder {
            check(this.evaluateListener == null) { "Only one onEvaluate can be set." }
            this.evaluateListener = evaluateListener
            return this
        }

        /**
         * Calling when all depend module be completed.
         * Can use depend module here.
         */
        fun onExecuted(block: (taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) -> Unit): P2MDriver.Builder {
            onExecuted(object : OnExecutedListener {
                override fun onExecuted(taskOutputProvider: TaskOutputProvider, moduleProvider: SafeModuleProvider) {
                    block(taskOutputProvider, moduleProvider)
                }
            })
            return this
        }

        /**
         * Calling when all depend module be completed.
         * Can use depend module here.
         */
        fun onExecuted(executeListener: OnExecutedListener): P2MDriver.Builder {
            check(this.executeListener == null) { "Only one onExecuted can be set." }
            this.executeListener = executeListener
            return this
        }

        fun build():P2MDriver{
            InternalP2MDriver.builder = this
            return InternalP2MDriver
        }

    }
}

interface P2MDriverState {

    var opened : Boolean

    var opening : Boolean
}
