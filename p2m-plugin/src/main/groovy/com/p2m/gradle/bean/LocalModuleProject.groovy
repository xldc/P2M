package com.p2m.gradle.bean


class LocalModuleProject extends ModuleProject {
    boolean runApp                      // 可以运行
    @Override
    String toString() {
        return "${getModuleName()}[ project(${project.path}) ]"
    }
}
