package com.p2m.gradle.bean


class RemoteModuleProjectUnit extends ModuleProjectUnit {
    @Override
    String toString() {
        return "Module ${getModuleName()}[remote aar(group=${groupId} version=${versionName})]"
    }
}
