package com.p2m.gradle.graph


class ModuleStage {
    ArrayList<ModuleNode> nodes
    
    boolean hasRing // 是否有环
    Map<ModuleNode, ModuleNode> ringNodes // 环节点
}
