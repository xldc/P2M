package com.p2m.core.internal.module

import com.p2m.core.app.APP_MODULE_NAME
import com.p2m.core.app.App
import com.p2m.core.internal.graph.Graph
import com.p2m.core.internal.graph.Stage
import com.p2m.core.internal.log.logI
import com.p2m.core.module.ModuleApi
import com.p2m.core.module.Module
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicInteger

@Suppress("SameParameterValue", "unused")
internal class ModuleGraph private constructor(private val topModule: AppModule) : Graph<ModuleNode, String> {
    private val modules: HashMap<String, Module> = HashMap()
    private val apiClasses: HashMap<String,Class<out ModuleApi<*, *, *>>> = HashMap()
    private val apis: HashMap<String, ModuleApi<*, *, *>> = HashMap()
    private val nodes: HashMap<String, ModuleNode> = HashMap()
    val moduleSize
        get() = modules.size
    override var stageSize = 0
    override var stageCompletedCount = AtomicInteger()

    companion object{
        internal fun fromTop(appModule: AppModule): ModuleGraph {
            return ModuleGraph(appModule)
        }
    }

    init {
        collectView()
    }

    private fun genTopModule(appModule: AppModule) {
        genModule(APP_MODULE_NAME, appModule)
        genApi(APP_MODULE_NAME, App())
        genApiClass(APP_MODULE_NAME, App::class.java)
    }

    private fun genModule(moduleName: String, module: Module) {
        modules[moduleName] = module
    }

    private fun genApi(moduleName: String, api: ModuleApi<*, *, *>) {
        apis[moduleName] = api
    }

    private fun genApiClass(moduleName: String, apiClass: Class<out ModuleApi<*, *, *>>) {
        apiClasses[moduleName] = apiClass
    }

    private fun addDepend(owner: String, depend: String) {
        val ownerNode = nodes[owner] ?: return
        val node = nodes[depend] ?: return

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

    override fun evaluate():HashMap<String, ModuleNode>{
        reset()
        layout()
        return nodes
    }

    private fun dependTop(){
        nodes.filter { it.value.byDependDegree == 0 && it.key != APP_MODULE_NAME}
            .keys
            .forEach { addDepend(APP_MODULE_NAME, it) }
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
        var count = 0
        while (!nodes.isEmpty()) {
            count++
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

    private fun collectView(){
        genTopModule(topModule)
        genModules()
        genApis()
        genApiClasses()
    }

    private fun layout(){
        genNodes()
        addDepends()
        dependTop()
    }

    private fun genModules() { }

    private fun genApis() { }

    private fun genApiClasses() { }

    private fun genNodes() {
        modules.iterator().forEach {
            val moduleName = it.key
            val module = it.value

            val api = apis[moduleName]
                ?: throw IllegalStateException("未知错误，每个模块包含一个模块和api模块")
            val apiClass = apiClasses[moduleName]
            val safeModuleProviderImpl = SafeModuleProviderImpl(topModule.context, moduleName, api)
            nodes[moduleName] = ModuleNode(moduleName, module, api, apiClass!!, safeModuleProviderImpl, module is AppModule)
        }
    }

    private fun addDepends() { }
}