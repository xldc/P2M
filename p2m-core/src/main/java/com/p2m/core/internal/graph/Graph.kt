package com.p2m.core.internal.graph

import java.util.concurrent.atomic.AtomicInteger

internal abstract class Graph<KEY, N:Node<N>> {

    var stageSize = 0
    var stageCompletedCount = AtomicInteger()

    abstract fun evaluate(): Map<KEY, N>

    fun eachStageBeginFromTail(block: (stage: Stage<N>) -> Unit) {
        val nodes = evaluate().values.toMutableList()
        while (nodes.isNotEmpty()) {
            val stage = Stage<N>()
            val noDependDegreeNodes = ArrayList<N>()

            nodes.forEach{ node ->
                if (node.dependDegree == 0) {
                    noDependDegreeNodes.add(node)
                }
            }

            if (noDependDegreeNodes.isEmpty()) {
                stage.hasRing = true
                stage.ringNodes = findRingNodes(nodes)
            }

            stageSize++
            stage.nodes = noDependDegreeNodes
            block(stage)

            noDependDegreeNodes.forEach { node: N ->
                node.byDependNodes.forEach { byDependNode: N ->
                    byDependNode.dependDegree--
                }
                nodes.remove(node)
            }
        }
    }

    fun eachStageBeginFromHead(block:(stage:Stage<N>)->Unit) {
        val nodes = evaluate().values.toMutableList()
        while (nodes.isNotEmpty()) {
            val stage = Stage<N>()
            val noByDependDegreeNodes = ArrayList<N>()

            nodes.forEach{ node ->
                if (node.byDependDegree == 0) {
                    noByDependDegreeNodes.add(node)
                }
            }

            if (noByDependDegreeNodes.isEmpty()) {
                stage.hasRing = true
                stage.ringNodes = findRingNodes(nodes)
            }

            stageSize++
            stage.nodes = noByDependDegreeNodes
            block(stage)

            noByDependDegreeNodes.forEach { node: N ->
                node.dependNodes.forEach { dependNode: N ->
                    dependNode.byDependDegree--
                }
                nodes.remove(node)
            }
        }
    }

    private fun findRingNodes(nodes: Collection<N>): HashMap<N, N> {
        val ringNodes = HashMap<N, N>()
        nodes.forEach { node: N ->
            node.dependNodes.forEach { dependNode: N ->
                if (dependNode.dependNodes.contains(node)) {
                    if (ringNodes[dependNode] != node) {
                        @Suppress("UNCHECKED_CAST")
                        ringNodes[node] = dependNode
                    }
                }
            }
        }
        return ringNodes
    }
}