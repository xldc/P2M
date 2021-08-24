package com.p2m.gradle.bean


abstract class ModuleProject extends BaseProject {
    boolean existModuleInitProxyImplClass
    boolean existApiClass

    String groupId

    String versionName

    String getModuleArtifactId(){
        return "p2m-module-${getModuleNameLowerCase()}"
    }

    String getApiArtifactId(){
        return "p2m-module-${getModuleNameLowerCase()}-api"
    }
}
