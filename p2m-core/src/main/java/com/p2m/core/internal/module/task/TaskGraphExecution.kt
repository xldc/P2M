package com.p2m.core.internal.module.task

import com.p2m.core.internal.graph.AbsGraphExecution
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.log.logI
import com.p2m.core.module.task.Task
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal class TaskGraphExecution(override val graph: TaskGraph): AbsGraphExecution<TaskNode, Class<out Task<*, *>>, TaskGraph>() {
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

    override fun runNode(node:TaskNode, onComplete:()->Unit) {
        if (node.isStartedConsiderNotifyCompleted(onComplete)) return

        // Started
        node.state = TaskNode.State.STARTED
        asyncTask(Runnable {
            // Depending
            node.state = TaskNode.State.DEPENDING
            node.depending()

            if (node.isTop) {
                node.state = TaskNode.State.COMPLETED
                onComplete()
                return@Runnable
            }

            // Executing
            node.state = TaskNode.State.EXECUTING
            node.executing()

            // Completed
            node.state = TaskNode.State.COMPLETED
            onComplete()
        })
    }

    private fun TaskNode.depending() {
        if (dependNodes.isEmpty()) {
            return
        }

        val countDownLatch = CountDownLatch(dependNodes.size)
        val complete = {
            // Sub node init be Completed.
            countDownLatch.countDown()
        }

        dependNodes.forEach { dependNode ->
            runNode(dependNode, complete)
        }

        // Wait sub node init be Completed.
        countDownLatch.await()
    }

    private fun TaskNode.executing() {
        logI("${graph.moduleName}-Task-Graph-Stage-Node-${taskName} onExecute()")
        task.inputObj = input
        task.onExecute(safeTaskProvider, graph.safeModuleProvider)
    }

    override fun asyncTask(runnable: Runnable) {
        executor.execute(runnable)
    }
    
    override fun onCompletedForGraph(graph: TaskGraph) {
        logI("${graph.moduleName}-Task-Graph Completed.")
    }

    override fun onCompletedForStage(stage: Stage<TaskNode>) {
        if(stage.nodes?.firstOrNull()?.isTop != true) {
            logI("${graph.moduleName}-Task-Graph-Stage${stage.name} Completed.")
        }
    }
    
    override fun onCompletedForNode(node: TaskNode) {
        if (node.task is TopTask) return
        logI("${graph.moduleName}-Task-Graph-Stage-Node-${node.taskName} Completed.")
    }
}