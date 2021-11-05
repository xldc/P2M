package com.p2m.core.internal.module

import android.content.Context
import com.p2m.core.internal.graph.Graph
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.log.logW
import com.p2m.core.module.Module
import java.util.concurrent.atomic.AtomicInteger

internal class ModuleGraph private constructor(
    private val context: Context,
    private val moduleContainer: ModuleContainerImpl,
    private val top: Module<*>
) : Graph<ModuleNode, Class<out Module<*>>> {
    private val nodes: HashMap<Class<out Module<*>>, ModuleNode> = HashMap()
    val moduleSize
        get() = moduleContainer.getAll().size
    override var stageSize = 0
    override var stageCompletedCount = AtomicInteger()

    companion object{
        internal fun create(context:Context, moduleContainer: ModuleContainerImpl, top: Module<*>): ModuleGraph {
            return ModuleGraph(context, moduleContainer, top)
        }
    }

    private fun Class<out Module<*>>.dependOn(dependClass: Class<out Module<*>>) {
        val ownerNode = nodes[this] ?: return
        val node = nodes[dependClass] ?: return

        if (node.byDependNodes.add(ownerNode)) {
            node.byDependDegree++
        }

        if (ownerNode.dependNodes.add(node)) {
            ownerNode.dependDegree++
        }
    }

    private fun findRingNodes(nodes: Collection<ModuleNode>): HashMap<ModuleNode, ModuleNode> {
        val ringNodes = HashMap<ModuleNode, ModuleNode>()
        nodes.forEach { node: ModuleNode ->
            node.dependNodes.forEach { dependNode: ModuleNode ->
                if (dependNode.dependNodes.contains(node)) {
                    if (ringNodes[dependNode] != node) {
                        ringNodes[node] = dependNode
                    }
                }
            }
        }
        return ringNodes
    }

    override fun evaluate():HashMap<Class<out Module<*>>, ModuleNode>{
        reset()
        createNodes()
        layout()
        return nodes
    }

    override fun getHeadStage(): Stage<ModuleNode> {
        val stage = Stage<ModuleNode>()
        val nodes = evaluate().values
        val noByDependDegreeNodes = ArrayList<ModuleNode>()
        nodes.forEach { node ->
            if (node.byDependDegree == 0) {
                noByDependDegreeNodes.add(node)
            }
        }
        stageSize = 1
        stage.nodes = noByDependDegreeNodes
        return stage
    }

    override fun getTailStage(): Stage<ModuleNode> {
        val stage = Stage<ModuleNode>()
        val nodes = evaluate().values
        val noDependDegreeNodes = ArrayList<ModuleNode>()
        nodes.forEach { node ->
            if (node.dependDegree == 0) {
                noDependDegreeNodes.add(node)
            }
        }
        stageSize = 1
        stage.nodes = noDependDegreeNodes
        return stage
    }
    
    override fun eachStageBeginFromTail(block: (stage: Stage<ModuleNode>) -> Unit) {
        val nodes = evaluate().values
        while (!nodes.isEmpty()) {
            val stage = Stage<ModuleNode>()
            val noDependDegreeNodes = ArrayList<ModuleNode>()

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

            noDependDegreeNodes.forEach { node: ModuleNode ->
                node.byDependNodes.forEach { byDependNode: ModuleNode ->
                    byDependNode.dependDegree--
                }
                nodes.remove(node)
            }
        }
    }
    
    override fun eachStageBeginFromHead(block:(stage:Stage<ModuleNode>)->Unit) {
        val nodes = evaluate().values
        while (!nodes.isEmpty()) {
            val stage = Stage<ModuleNode>()
            val noByDependDegreeNodes = ArrayList<ModuleNode>()

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

            noByDependDegreeNodes.forEach { node: ModuleNode ->
                node.dependNodes.forEach { dependNode: ModuleNode ->
                    dependNode.byDependDegree--
                }
                nodes.remove(node)
            }
        }
    }

    private fun reset(){
        resetNode()
        resetStage()
    }

    private fun resetNode(){
        nodes.clear()
    }

    private fun resetStage(){
        stageSize = 0
        stageCompletedCount.set(0)
    }
    
    private fun layout(){
        depends()
    }

    private fun createNodes() {
        createNodeRecursive(top, true)
    }

    private fun createNodeRecursive(module: Module<*>, isTop: Boolean) {
        val moduleUnit = module.internalModuleUnit
        val safeModuleProviderImpl = SafeModuleApiProviderImpl(moduleContainer, module)
        nodes[moduleUnit.moduleImplClass] = ModuleNode(
            context,
            module,
            safeModuleProviderImpl,
            isTop
        )

        for (dependency in moduleUnit.getDependencies()) {
            val moduleDependency = moduleContainer.find(dependency)!!
            createNodeRecursive(moduleDependency, false)
        }
    }
    
    private fun depends() {
        for (node in nodes) {
            val owner = node.key
            moduleContainer.find(owner)?.run {
                for (dependency in internalModuleUnit.getDependencies()) {
                    if (!nodes.containsKey(dependency)) {
                        logW("${owner.canonicalName} depend on ${dependency.canonicalName}, but not registered of ${dependency.canonicalName}")
                    }else {
                        owner.dependOn(dependency)
                    }
                }
            }
        }
    }

}