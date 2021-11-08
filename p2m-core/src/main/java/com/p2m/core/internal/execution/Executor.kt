package com.p2m.core.internal.execution

internal interface Executor{

    fun runningAndLoop(direction: BeginDirection)

    fun postTask(runnable: Runnable)

    fun asyncTask(runnable: Runnable)

    fun quitLoop(runnable: Runnable)
}