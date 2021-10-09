package com.p2m.gradle.bean


class RemoteModuleProject extends ModuleProject {
    @Override
    String toString() {
        return "Module ${getModuleName()}[remote aar(group=${groupId} version=${versionName})]"
    }
}
