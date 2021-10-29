package com.p2m.compiler.bean

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock

data class GenResult(
    val apiClassName: ClassName,
    val implClassName: ClassName,
    val implIsObjectInstance : Boolean = false
){
    fun getImplInstanceStatement(): CodeBlock {
        return if (implIsObjectInstance) {
            CodeBlock.of("%T", implClassName)
        }else{
            CodeBlock.of("%T()", implClassName)
        }
    }
}