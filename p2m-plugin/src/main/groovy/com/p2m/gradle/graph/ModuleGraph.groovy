package com.p2m.gradle.graph

import com.p2m.gradle.bean.Named
import org.gradle.api.Action

class ModuleGraph {
    Map<Named, ModuleNode> nodes = new HashMap()
    ModuleNode topNode

    def addDepend = { Named owner, Named dependency ->
        ModuleNode ownerNode = moduleNode(owner)

        ModuleNode node = moduleNode(dependency)
        if (node.byDependNodes.add(ownerNode)) {
            node.byDependDegree++
        }

       if (ownerNode.dependNodes.add(node)) {
           ownerNode.dependDegree++
       }
    }

    def addDepends = { Named owner, Set<Named> dependencies ->
        ModuleNode ownerNode = moduleNode(owner)
        if (dependencies == null || dependencies.isEmpty()) return
        dependencies.forEach { Named named->
            ModuleNode node = moduleNode(named)
            if (node.byDependNodes.add(ownerNode)) {
                node.byDependDegree++
            }

            if (ownerNode.dependNodes.add(node)) {
                ownerNode.dependDegree++
            }
        }
    }
    
    def setTopNode = { Named named ->
        this.topNode = moduleNode(named)
    }

    def moduleNode = { Named named ->
        def node = nodes[named]
        if (node == null) {
            node = new ModuleNode()
            node.named = named
            nodes[named] = node
        }
        return node
    }


    private Map<Named, ModuleNode> cloneNodes = new HashMap()

    private def cloneModuleNode = { Named named ->
        def node = cloneNodes[named]
        if (node == null) {
            node = nodes[named].clone()
            node.named = named
            cloneNodes[named] = node
        }
        return node
    }

    private Collection<ModuleNode> cloneNodes(){
        cloneNodes.clear()

        nodes.values().forEach{ ModuleNode node ->
            ModuleNode clone = cloneModuleNode(node.named)

            node.dependNodes.forEach{ n->
                clone.dependNodes.add(cloneModuleNode(n.named))
            }

            node.byDependNodes.forEach{ n->
                clone.byDependNodes.add(cloneModuleNode(n.named))
            }

        }
        return cloneNodes.values()
    }

    def findRingNodes = { Collection<ModuleNode> nodes ->
        HashMap<ModuleNode, ModuleNode> ringNodes = new HashMap()
        nodes.forEach { ModuleNode node ->
            node.dependNodes.forEach { ModuleNode dependNode ->
                if (dependNode.dependNodes.contains(node)) {
                    if (ringNodes[dependNode] != node) {
                        ringNodes[node] = dependNode
                    }
                }
            }
        }
        return ringNodes
    }

    def eachStageBeginFromHead(Action<ModuleStage> action) {
        Collection<ModuleNode> nodes = cloneNodes()
        while (!nodes.isEmpty()) {
            def stage = new ModuleStage()
            List<ModuleNode> noByDependDegreeNodes = new ArrayList()
            nodes.forEach{ node ->
                if (node.byDependDegree == 0) {
                    noByDependDegreeNodes.add(node)
                }
            }

            if (noByDependDegreeNodes.isEmpty()) {
                stage.hasRing = true
                stage.ringNodes = findRingNodes(nodes)
            }

            stage.nodes = noByDependDegreeNodes
            action.execute(stage)

            noByDependDegreeNodes.forEach{ ModuleNode node ->
                node.dependNodes.forEach { ModuleNode dependNode ->
                    dependNode.byDependDegree--
                }
                nodes.remove(node)
            }
        }
    }


    def eachStageBeginFromTail(Action<ModuleStage> action) {
        Collection<ModuleNode> nodes = cloneNodes()
        while (!nodes.isEmpty()) {
            def stage = new ModuleStage()
            List<ModuleNode> noDependDegreeNodes = new ArrayList()
            nodes.forEach{ node ->
                if (node.dependDegree == 0) {
                    noDependDegreeNodes.add(node)
                }
            }

            if (noDependDegreeNodes.isEmpty()) {
                stage.hasRing = true
                stage.ringNodes = findRingNodes(nodes)
            }

            stage.nodes = noDependDegreeNodes
            action.execute(stage)

            noDependDegreeNodes.forEach{ ModuleNode node ->
                node.byDependNodes.forEach { ModuleNode byDependNode ->
                    byDependNode.dependDegree--
                }
                nodes.remove(node)
            }
        }
    }
}
/*
                moduleGraph.eachStageBeginFromTail { ModuleStage stage ->

                    if (stage.hasRing) {
                        def builder = new StringBuilder()
                        stage.ringNodes.forEach {node, dependNode->
                            builder.append("\n" +
                                    "${NamedUtils.getStatement(node.named)} " +
                                    "and " +
                                    "${NamedUtils.getStatement(dependNode.named)}"
                            )
                        }
                        throw new IllegalStateException("Prohibit interdependence between modules. Please check with setting.gradle: ${builder.toString()}")
                    }

                    stage.nodes.each {node ->
                        // print("配置：${node.named.get()},")
                    }
                }
* */
