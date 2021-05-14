package com.p2m.gradle.bytecode

import com.p2m.gradle.bean.BaseProject
import com.p2m.gradle.bean.ModuleProject
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class GenApiClassesMethodVisitor extends AdviceAdapter {
    HashMap<String, BaseProject> p2mProject

    protected GenApiClassesMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, HashMap<String, BaseProject> p2mProject) {
        super(api, methodVisitor, access, name, descriptor)
        this.p2mProject = p2mProject
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        p2mProject.forEach{projectName, moduleProject->
            // 仅限模块
            if (!(moduleProject instanceof ModuleProject)) return

            def existApiClass = ((ModuleProject) moduleProject).existApiClass

            // println("onMethodEnter -> " + moduleProject.getModuleName() + ": " + name + "  existApiClass：" + existApiClass)

            def apiTypeName =
                    existApiClass ?
                            "Lcom/p2m/module/api/${moduleProject.getModuleName()};"
                            : "Lcom/p2m/core/module/EmptyModuleApi;"

            mv.visitVarInsn(ALOAD, 0)
            mv.visitLdcInsn(moduleProject.getModuleName())
            mv.visitLdcInsn(Type.getType(apiTypeName))
            mv.visitMethodInsn(INVOKEVIRTUAL, "com/p2m/core/internal/module/ModuleGraph", "genApiClass", "(Ljava/lang/String;Ljava/lang/Class;)V", false)
        }
    }

}
