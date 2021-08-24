package com.p2m.core.internal.module

import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.graph.AbsGraphExecution
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.module.task.TaskOutputProviderImplForModule
import com.p2m.core.internal.module.task.TaskGraph
import com.p2m.core.internal.module.task.TaskGraphExecution
import com.p2m.core.module.EmptyModuleInit
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal class ModuleGraphExecution(override val graph: ModuleGraph, private val moduleProvider: InnerModuleManager): AbsGraphExecution<ModuleNode, String, ModuleGraph>() {

    private val executor: ExecutorService =  ThreadPoolExecutor(
            0,
            Int.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            SynchronousQueue<Runnable>(),
            object : ThreadFactory {
                private val THREAD_NAME = "p2m_module_graph_%d"
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

    override val messageQueue: BlockingQueue<Runnable> = ArrayBlockingQueue<Runnable>(graph.moduleSize)

    override fun runNode(node: ModuleNode, onDependsNodeComplete: () -> Unit) {
        if (node.isStartedConsiderNotifyCompleted(onDependsNodeComplete)) return

        // Started
        node.state = ModuleNode.State.STARTED

        asyncTask(Runnable {

            val evaluatingWhenDependingIdle = {
                // Evaluating
                node.state = ModuleNode.State.EVALUATING
                node.evaluating()
            }

            // Depending
            node.state = ModuleNode.State.DEPENDING
            node.depending(evaluatingWhenDependingIdle)

            if (node.moduleInit is EmptyModuleInit) { // empty
                node.executed()
                // Completed
                node.state = ModuleNode.State.COMPLETED
                onDependsNodeComplete()
                return@Runnable
            }

            // Executing
            node.state = ModuleNode.State.EXECUTING
            node.executing()
            postTask(Runnable {
                node.executed()
                // Completed
                node.state = ModuleNode.State.COMPLETED
                onDependsNodeComplete()
            })
        })
    }

    private fun ModuleNode.executing() {
        if (!taskContainer.onlyHasTop) {
            val taskGraph = TaskGraph.create(moduleName, taskContainer, provider)
            val taskGraphExecution = TaskGraphExecution(taskGraph)
            taskGraphExecution.runningAndLoop(BeginDirection.TAIL)
        }
    }

    private fun ModuleNode.evaluating() {
        logI("Module-Graph-Node-$moduleName onEvaluate()")
        moduleInit.onEvaluate(taskContainer)
    }

    private fun ModuleNode.depending(evaluatingWhenDependingIdle: () -> Unit) {
        if (dependNodes.isEmpty()) {
            evaluatingWhenDependingIdle()
            return
        }

        val countDownLatch = CountDownLatch(dependNodes.size)
        val onDependsNodeComplete = {
            // Depends node be Completed.
            countDownLatch.countDown()
        }

        dependNodes.forEach { dependNode ->
            runNode(dependNode, onDependsNodeComplete)
        }

        evaluatingWhenDependingIdle()

        // Wait Depends node be Completed.
        countDownLatch.await()
    }

    private fun ModuleNode.executed() {
        logI("Module-Graph-Node-$moduleName onExecuted()")
        moduleInit.onExecuted(TaskOutputProviderImplForModule(taskContainer), provider)
        moduleProvider.registerModule(apiClass, api)
    }

    override fun asyncTask(runnable: Runnable) {
        executor.execute(runnable)
    }

    override fun onCompletedForGraph(graph: ModuleGraph) {
        logI("Module-Graph Completed.")
    }
    
    override fun onCompletedForStage(stage: Stage<ModuleNode>) {
        // logI("Module-Graph-Stage${stage.name} Completed.")
    }

    override fun onCompletedForNode(node: ModuleNode) {
        logI("Module-Graph-Node-${node.moduleName} Completed.")
    }

}