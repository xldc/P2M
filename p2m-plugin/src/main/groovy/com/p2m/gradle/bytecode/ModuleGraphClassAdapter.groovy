package com.p2m.gradle.bytecode

import com.p2m.gradle.bean.BaseProject
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class ModuleGraphClassAdapter extends ClassVisitor {
    HashMap<String, BaseProject> p2mProject
    ModuleGraphClassAdapter(ClassVisitor classVisitor, HashMap<String, BaseProject> p2mProject) {
        super(Opcodes.ASM7, classVisitor)
        this.p2mProject = p2mProject
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name == "genModules") {
            // println("visitMethod -> genModules")
            def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return new GenModulesMethodVisitor(api, methodVisitor, access, name, descriptor, p2mProject)
        } else if (name == "genApis") {
            // println("visitMethod -> genApis")
            def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return new GenApisMethodVisitor(api, methodVisitor, access, name, descriptor, p2mProject)
        }  else if (name == "genApiClasses") {
            // println("visitMethod -> genApiClasses")
            def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return new GenApiClassesMethodVisitor(api, methodVisitor, access, name, descriptor, p2mProject)
        } else if (name == "addDepends") {
            // println("visitMethod -> addDepends")
            def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return new AddDependsMethodVisitor(api, methodVisitor, access, name, descriptor, p2mProject)
        }else {
            super.visitMethod(access,name,descriptor,signature,exceptions)
        }
    }
}
