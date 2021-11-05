package com.p2m.core.internal.module.deriver

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.p2m.core.module.driver.Driver
import com.p2m.core.module.driver.Driver.State
import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.module.ModuleContainerImpl
import com.p2m.core.internal.module.ModuleGraph
import com.p2m.core.internal.module.ModuleGraphExecution
import com.p2m.core.module.Module
import com.p2m.core.module.SafeModuleApiProvider
import java.util.concurrent.locks.ReentrantLock

internal class InternalDriver(
    private val context: Context,
    private val topModule: Module<*>,
    private val moduleContainer: ModuleContainerImpl
) : Driver{
    @Volatile
    var state: State = State.NEW
    private val lock = ReentrantLock()
    private val openCondition = lock.newCondition()
    internal var isEvaluating : ThreadLocal<Boolean>? = null
    internal var executingModuleProvider : ThreadLocal<SafeModuleApiProvider>? = null
    private val moduleGraph by lazy(LazyThreadSafetyMode.NONE) {
        ModuleGraph.create(context, moduleContainer, topModule)
    }

    override fun considerOpenAwait() {
        if (state === State.OPENED) return
        lock.lock()
        try {
            when (state) {
                State.NEW -> {
                    state = State.OPENING
                    val mainLooper = Looper.getMainLooper()
                    if (mainLooper === Looper.myLooper()) {
                        openReal()
                    }else {
                        Handler(mainLooper).post { openReal() }
                        openCondition.await()
                    }
                }
                State.OPENING -> openCondition.await()
                State.OPENED -> return
            }
        } finally {
            lock.unlock()
        }
    }

    private fun openReal() {
        isEvaluating = ThreadLocal()
        executingModuleProvider = ThreadLocal()
        val moduleGraphExecution = ModuleGraphExecution(moduleGraph, isEvaluating!!, executingModuleProvider!!)
        moduleGraphExecution.runningAndLoop(BeginDirection.TAIL)
        isEvaluating = null
        executingModuleProvider = null
        state = State.OPENED
        openCondition.signalAll()
    }
}