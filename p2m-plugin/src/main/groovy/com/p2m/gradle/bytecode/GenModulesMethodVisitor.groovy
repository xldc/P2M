package com.p2m.gradle.bytecode

import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class GenModulesMethodVisitor extends AdviceAdapter {
    HashMap<String, BaseProject> p2mProject

    protected GenModulesMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, BaseProject> p2mProject) {
        super(api, methodVisitor, access, name, descriptor)
        this.p2mProject = p2mProject
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        p2mProject.forEach{projectName, moduleProject->
            // 仅限模块
            if (!(moduleProject instanceof ModuleProject)) return

            def existModuleProxyImplClass = ((ModuleProject) moduleProject).existModuleProxyImplClass

            // println("onMethodEnter -> " + moduleProject.getModuleName() + ": " + name + "  existModuleProxyImplClass：" + existModuleProxyImplClass)
            def moduleTypeName =
                    existModuleProxyImplClass ?
                            "com/p2m/module/impl/_${moduleProject.getModuleName()}Module"
                            : "com/p2m/core/module/EmptyModule"
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(moduleProject.getModuleName())
            mv.visitTypeInsn(NEW, moduleTypeName)
            mv.visitInsn(DUP)
            mv.visitMethodInsn(INVOKESPECIAL, moduleTypeName, "<init>", "()V", false)
            mv.visitMethodInsn(INVOKESPECIAL, "com/p2m/core/internal/module/ModuleGraph", "genModule", "(Ljava/lang/String;Lcom/p2m/core/module/Module;)V", false)
        }
    }

}
