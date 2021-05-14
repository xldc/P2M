package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenModuleServiceResult (
    val serviceInterfaceClassName: ClassName,
    val serviceImplClassName: ClassName,
    val proxyIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (proxyIsObjectInstance) {
            CodeBlock.of("%T", serviceImplClassName)
        }else{
            CodeBlock.of("%T()", serviceImplClassName)
        }
    }
}