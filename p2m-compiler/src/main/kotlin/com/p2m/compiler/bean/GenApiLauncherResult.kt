package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenModuleLauncherResult (
    val launcherInterfaceClassName: ClassName,
    val launcherImplClassName: ClassName,
    val proxyIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (proxyIsObjectInstance) {
            CodeBlock.of("%T", launcherImplClassName)
        }else{
            CodeBlock.of("%T()", launcherImplClassName)
        }
    }
}