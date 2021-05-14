package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenModuleApiResult(
    val moduleApiClassName: ClassName,
    val moduleApiImplClassName: ClassName,
    val proxyIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (proxyIsObjectInstance) {
            CodeBlock.of("%T", moduleApiImplClassName)
        }else{
            CodeBlock.of("%T()", moduleApiImplClassName)
        }
    }
}