package com.p2m.gradle.bean


abstract class ModuleProject extends BaseProject {
    boolean existModuleClass

    String groupId

    String versionName

    String getModuleArtifactId(){
        return "p2m-${getModuleNameLowerCase()}-module"
    }

    String getApiArtifactId(){
        return "p2m-${getModuleNameLowerCase()}-module-api"
    }
}
