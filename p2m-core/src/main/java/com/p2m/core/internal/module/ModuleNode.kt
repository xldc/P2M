package com.p2m.core.internal.module

import android.content.Context
import com.p2m.core.internal.graph.Node
import com.p2m.core.internal.module.task.DefaultTaskFactory
import com.p2m.core.internal.module.task.TaskContainerImpl
import com.p2m.core.internal.module.task.TopTask
import com.p2m.core.module.Module

internal class ModuleNode constructor(
    val context: Context,
    val module: Module<*, *>,
    val provider: SafeModuleApiProviderImpl,
    override val isTop: Boolean
) : Node {

    override val name: String
        get() = module.apiClazzName

    // 被依赖
    var byDependDegree: Int = 0
    var byDependNodes: HashSet<ModuleNode> = HashSet()

    // 依赖
    var dependDegree: Int = 0
    var dependNodes: HashSet<ModuleNode> = HashSet()

    var stateLock = Any()

    val taskContainer by lazy(LazyThreadSafetyMode.NONE) { TaskContainerImpl(TopTask::class.java, DefaultTaskFactory()) }
    
    @Volatile
    var state: State = State.NONE
        set(value) {
            synchronized(stateLock) {
                // logI("module[$moduleName] init ${value.name}")

                field = value

                if (value == State.COMPLETED) {
                    for (completedListener in completedListeners) {
                        completedListener.run()
                    }
                }
            }
        }
    get() {
        synchronized(stateLock) {
            return field
        }
    }

    private val completedListeners = arrayListOf<Runnable>()

    fun addOnCompleted(runner:Runnable) {
        completedListeners.add(runner)
    }
    
    fun nextStateShouldIs(targetState: State): Boolean {
        synchronized(stateLock) {
            return targetState.compareTo(this.state) == 1
        }
    }
    
    fun currentStateShouldIs(targetState: State): Boolean {
        synchronized(stateLock) {
            return targetState == state
        }
    }

    fun isStartedConsiderNotifyCompleted(onComplete: () -> Unit): Boolean {
        synchronized(stateLock) {
            if (state != State.NONE) {
                if (state == State.COMPLETED) {
                    onComplete()
                } else {
                    addOnCompleted(Runnable {
                        onComplete()
                    })
                }
                return true
            }
            return false
        }
    }

    enum class State {
        NONE,
        STARTED,
        EVALUATING,
        DEPENDING,
        EXECUTING,
        COMPLETED
    }
}