package com.p2m.core.internal.module.task

import com.p2m.core.internal.graph.AbsGraphExecutor
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.graph.Node.State
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.module.SafeModuleApiProvider
import com.p2m.core.module.task.Task
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal class TaskGraphExecutor(override val graph: TaskGraph, private val executingModuleProvider: ThreadLocal<SafeModuleApiProvider>) :
    AbsGraphExecutor<Class<out Task<*, *>>, TaskNode, TaskGraph>() {

    companion object{
        private val EXECUTOR: ExecutorService =  ThreadPoolExecutor(
            0,
                Int.MAX_VALUE,
                60,
                TimeUnit.SECONDS,
                SynchronousQueue<Runnable>(),
                object : ThreadFactory {
                    private val THREAD_NAME = "p2m_task_graph_%d"
                    private val threadId = AtomicInteger(0)
                    override fun newThread(r: Runnable): Thread {
                        val t = Thread(r)
                        t.isDaemon = false
                        t.priority = Thread.NORM_PRIORITY
                        t.name = String.format(THREAD_NAME, threadId.getAndIncrement())
                        return t
                    }

                }
        )
    }

    private val executor: ExecutorService = EXECUTOR

    override val messageQueue: BlockingQueue<Runnable> = ArrayBlockingQueue<Runnable>(graph.taskSize)

    override fun runNode(node: TaskNode, onDependsNodeComplete: () -> Unit) {
        node.markStarted(onDependsNodeComplete) {
            // Started
            asyncTask(Runnable {
                // Depending
                node.depending()

                if (node.isTop) {
                    node.executed()
                    onDependsNodeComplete()
                    return@Runnable
                }

                // Executing
                node.executing()

                // Completed
                node.executed()
                onDependsNodeComplete()
            })
        }
    }

    private fun TaskNode.executed() {
        markCompleted()
    }

    private fun TaskNode.depending() {
        mark(State.DEPENDING)
        if (dependNodes.isEmpty()) {
            return
        }

        val countDownLatch = CountDownLatch(dependNodes.size)
        val onDependsNodeComplete = {
            // Dependencies be Completed.
            countDownLatch.countDown()
        }

        dependNodes.forEach { dependNode ->
            runNode(dependNode, onDependsNodeComplete)
        }

        // Wait dependencies be Completed.
        countDownLatch.await()
    }

    private fun TaskNode.executing() {
        mark(State.EXECUTING)
        logI("${graph.moduleName}-Task-${taskName} `onExecute()`")
        task.inputObj = input
        executingModuleProvider.set(graph.SafeModuleApiProvider)
        task.onExecute(context, safeTaskProvider)
        executingModuleProvider.set(null)
    }

    override fun asyncTask(runnable: Runnable) {
        executor.execute(runnable)
    }

    override fun onCompletedForGraph(graph: TaskGraph) {
        logI("${graph.moduleName}-Task Completed.")
    }

    override fun onCompletedForStage(stage: Stage<TaskNode>) {
        // if (stage.nodes?.firstOrNull()?.isTop != true) {
        //     logI("${graph.moduleName}-Task-Graph-Stage${stage.name} Completed.")
        // }
    }

    override fun onCompletedForNode(node: TaskNode) {
//        if (node.task is TopTask) return
//        logI("${graph.moduleName}-Task-${node.taskName} Completed.")
    }
}