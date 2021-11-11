package com.p2m.gradle.bean


abstract class ModuleProjectUnit extends BaseProjectUnit {
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
