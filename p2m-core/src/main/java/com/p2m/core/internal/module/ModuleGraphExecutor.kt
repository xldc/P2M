package com.p2m.core.internal.module

import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.graph.AbsGraphExecutor
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.graph.Node.State
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.module.task.TaskOutputProviderImplForModule
import com.p2m.core.internal.module.task.TaskGraph
import com.p2m.core.internal.module.task.TaskGraphExecutor
import com.p2m.core.module.Module
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal class ModuleGraphExecutor(
    override val graph: ModuleGraph,
    private val isEvaluating: ThreadLocal<Boolean>,
    private val executingModuleProvider: ThreadLocal<SafeModuleApiProvider>
) : AbsGraphExecutor<Class<out Module<*>>, ModuleNode, ModuleGraph>() {

    companion object {
        private val executor: ExecutorService = ThreadPoolExecutor(
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
    }

    override val messageQueue: BlockingQueue<Runnable> = ArrayBlockingQueue<Runnable>(graph.moduleSize)

    override fun runNode(node: ModuleNode, onDependsNodeComplete: () -> Unit) {
        node.markStarted(onDependsNodeComplete) {
            // Started
            asyncTask(Runnable {
                val evaluatingWhenDependingIdle = {
                    // Evaluating
                    node.evaluating()
                }

                // Depending
                node.depending(evaluatingWhenDependingIdle)

                // Executing
                node.executing()
                postTask(Runnable {
                    // Completed
                    node.markSelfAvailable()
                    node.executed()
                    onDependsNodeComplete()
                })
            })
        }
    }

    private fun ModuleNode.executing() {
        mark(State.EXECUTING)
        if (!taskContainer.onlyHasTop) {
            val taskGraph = TaskGraph.create(context, name, taskContainer, safeModuleApiProvider)
            val taskGraphExecution = TaskGraphExecutor(taskGraph, executingModuleProvider)
            taskGraphExecution.runningAndLoop(BeginDirection.TAIL)
        }
    }

    private fun ModuleNode.evaluating() {
        mark(State.EVALUATING)
        logI("$name `onEvaluate()`")
        isEvaluating.set(true)
        module.internalInit.onEvaluate(context, taskContainer)
        isEvaluating.set(false)
    }

    private fun ModuleNode.depending(evaluatingWhenDependingIdle: () -> Unit) {
        mark(State.DEPENDING)
        if (dependNodes.isEmpty()) {
            evaluatingWhenDependingIdle()
            return
        }

        val countDownLatch = CountDownLatch(dependNodes.size)
        val onDependenciesComplete = {
            // Dependencies be Completed.
            countDownLatch.countDown()
        }

        dependNodes.forEach { dependNode ->
            runNode(dependNode, onDependenciesComplete)
        }

        evaluatingWhenDependingIdle()

        // Wait dependencies be Completed.
        countDownLatch.await()
    }

    private fun ModuleNode.executed() {
        logI("$name `onExecuted()`")
        val taskOutputProviderImplForModule = TaskOutputProviderImplForModule(taskContainer)

        executingModuleProvider.set(safeModuleApiProvider)
        module.internalInit.onExecuted(context, taskOutputProviderImplForModule)
        executingModuleProvider.set(null)
        markCompleted()
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
//        logI("${node.name} Completed.")
    }

}