package com.p2m.gradle.bean


class RemoteModuleProject extends ModuleProject {
    @Override
    String toString() {
        return "${getModuleName()}[ aar(group=${groupId} version=${versionName}) ]"
    }
}
