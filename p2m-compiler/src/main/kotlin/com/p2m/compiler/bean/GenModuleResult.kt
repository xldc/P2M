package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenModuleResult (
    val moduleImplClassName: ClassName,
    val proxyIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (proxyIsObjectInstance) {
            CodeBlock.of("%T", moduleImplClassName)
        }else{
            CodeBlock.of("%T()", moduleImplClassName)
        }
    }
}