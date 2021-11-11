package com.p2m.gradle

import com.p2m.gradle.bean.BaseProjectUnit
import com.p2m.gradle.bean.ModuleProjectUnit
import com.p2m.gradle.bean.RunAppConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BaseModulePlugin implements Plugin<Project> {
    protected BaseProjectUnit projectUnit
    protected RunAppConfig runAppConfig
    protected Set<ModuleProjectUnit> moduleDependencies

    @Override
    void apply(final Project project) {
        projectUnit = project.p2mProject
        runAppConfig = projectUnit.runAppConfig
        moduleDependencies = projectUnit.dependencies
        doAction(project)
    }

    abstract void doAction(Project project)
}
