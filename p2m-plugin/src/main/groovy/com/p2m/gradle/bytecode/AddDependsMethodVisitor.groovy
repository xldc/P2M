package com.p2m.gradle.bytecode

import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.LocalModuleProject
import com.p2m.gradle.bean.AppProject
import com.p2m.gradle.bean.ModuleProject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class AddDependsMethodVisitor extends AdviceAdapter {
    HashMap<String, BaseProject> p2mProject

    protected AddDependsMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, BaseProject> p2mProject) {
        super(api, methodVisitor, access, name, descriptor)
        this.p2mProject = p2mProject
    }

    //方法进入
    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        p2mProject.forEach{projectName, moduleProject->

            boolean isApp = moduleProject instanceof AppProject
            String moduleName = isApp ? "App" : moduleProject.getModuleName()
            // println("onMethodEnter -> " + moduleProject.getModuleName() + ": " + name + "  moduleName：" + moduleName)
            moduleProject.dependencies.forEach{ ModuleProject dependModuleProject->
                mv.visitVarInsn(ALOAD, 0)
                mv.visitLdcInsn(moduleName)
                mv.visitLdcInsn(dependModuleProject.getModuleName())
                mv.visitMethodInsn(INVOKESPECIAL, "com/p2m/core/internal/module/ModuleGraph", "addDepend", "(Ljava/lang/String;Ljava/lang/String;)V", false)
            }

            if (moduleProject instanceof LocalModuleProject) {
                if (moduleProject.runApp) {
                    println("onMethodEnter -> " + moduleProject.getModuleName() + ": " + name + "  runApp=true  App depend on self of " + moduleName)
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitLdcInsn("App")
                    mv.visitLdcInsn(moduleProject.getModuleName())
                    mv.visitMethodInsn(INVOKESPECIAL, "com/p2m/core/internal/module/ModuleGraph", "addDepend", "(Ljava/lang/String;Ljava/lang/String;)V", false)
                }
            }
        }
    }

}
