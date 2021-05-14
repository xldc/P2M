package com.p2m.gradle.utils

import com.p2m.gradle.bean.ModuleNamed
import com.p2m.gradle.bean.Named
import com.p2m.gradle.bean.ProjectNamed

class NamedUtils {
    static def module = { name ->
        return new ModuleNamed(name)
    }
    
    static def project = { name ->
        return new ProjectNamed(name)
    }
    
    static def getStatement = { Named named->
        if (named instanceof ModuleNamed) {
            return "module(\"${named.get()}\")"
        }else if (named instanceof ProjectNamed){
            return "project(\"${named.getInclude()}\")"
        }else {
            return named.get()
        }
    }
}
