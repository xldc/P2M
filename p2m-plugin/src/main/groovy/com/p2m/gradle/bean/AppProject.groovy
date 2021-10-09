package com.p2m.gradle.bean

class AppProject extends BaseProject {
    @Override
    String toString() {
        return "${getModuleName()}[${project.path}]"
    }
}
