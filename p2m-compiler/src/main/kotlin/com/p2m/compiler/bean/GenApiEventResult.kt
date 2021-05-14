package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenModuleEventResult (
    val eventInterfaceClassName: ClassName,
    val eventImplClassName: ClassName,
    val proxyIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (proxyIsObjectInstance) {
            CodeBlock.of("%T", eventImplClassName)
        }else{
            CodeBlock.of("%T()", eventImplClassName)
        }
    }
}