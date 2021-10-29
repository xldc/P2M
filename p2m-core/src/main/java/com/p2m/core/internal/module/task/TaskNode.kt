package com.p2m.core.internal.module.task

import android.content.Context
import com.p2m.core.internal.graph.Node
import com.p2m.core.module.task.Task

internal class TaskNode constructor(
    val context: Context,
    val taskName: String,
    val task: Task<*, *>,
    val input: Any?,
    val safeTaskProvider: TaskOutputProviderImplForTask,
    override val isTop: Boolean
) : Node {

    override val name: String
        get() = taskName

    // 被依赖
    var byDependDegree: Int = 0
    var byDependNodes: HashSet<TaskNode> = HashSet()

    // 依赖
    var dependDegree: Int = 0
    var dependNodes: HashSet<TaskNode> = HashSet()

    var stateLock = Any()

    @Volatile
    var state: State = State.NONE
        set(value) {
            synchronized(stateLock) {

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
        DEPENDING,
        EXECUTING,
        COMPLETED
    }
}