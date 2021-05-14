package com.p2m.core.internal.execution

internal interface Execution{
    /**
     * 运行并轮询消息
     */
    fun runningAndLoop(direction: BeginDirection)

    /**
     * 提交任务
     */
    fun postTask(runnable: Runnable)

    /**
     * 异步任务
     */
    fun asyncTask(runnable: Runnable)

    /**
     * 通知入口完成，停止阻塞
     */
    fun quitLoop(runnable: Runnable)
}