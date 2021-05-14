package com.p2m.gradle.graph

import com.p2m.gradle.bean.Named


class ModuleNode {
    Named named
    
    // 被依赖的Degree
    int byDependDegree = 0
    Set<ModuleNode> byDependNodes = new HashSet()
    
    // 依赖的Degree
    int dependDegree = 0
    Set<ModuleNode> dependNodes = new HashSet()

    @Override
    int hashCode() {
        return named.hashCode()
    }

    ModuleNode clone(){
        ModuleNode node = new ModuleNode()
        node.named = named
        node.byDependDegree = byDependDegree
        node.dependDegree = dependDegree
        return node
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof ModuleNode)  {
            return false
        }
        return named.equals((o as ModuleNode).named)
    }
}
