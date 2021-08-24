package com.p2m.gradle.bytecode

import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class GenModuleInitsMethodVisitor extends AdviceAdapter {
    HashMap<String, BaseProject> p2mProject

    protected GenModuleInitsMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, BaseProject> p2mProject) {
        super(api, methodVisitor, access, name, descriptor)
        this.p2mProject = p2mProject
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        p2mProject.forEach{projectName, moduleProject->
            // 仅限模块
            if (!(moduleProject instanceof ModuleProject)) return

            def existModuleInitProxyImplClass = ((ModuleProject) moduleProject).existModuleInitProxyImplClass

            // println("onMethodEnter -> " + moduleProject.getModuleName() + ": " + name + "  existModuleInitProxyImplClass：" + existModuleInitProxyImplClass)
            def moduleInitTypeName =
                    existModuleInitProxyImplClass ?
                            "com/p2m/module/impl/_${moduleProject.getModuleName()}ModuleInit"
                            : "com/p2m/core/module/EmptyModuleInit"
            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(moduleProject.getModuleName())
            mv.visitTypeInsn(NEW, moduleInitTypeName)
            mv.visitInsn(DUP)
            mv.visitMethodInsn(INVOKESPECIAL, moduleInitTypeName, "<init>", "()V", false)
            mv.visitMethodInsn(INVOKESPECIAL, "com/p2m/core/internal/module/ModuleGraph", "genModuleInit", "(Ljava/lang/String;Lcom/p2m/core/module/ModuleInit;)V", false)
        }
    }

}
