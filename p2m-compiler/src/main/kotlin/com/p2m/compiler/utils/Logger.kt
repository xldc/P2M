package com.p2m.compiler.utils

import javax.tools.Diagnostic
import javax.annotation.processing.Messager


class Logger(private val msg: Messager) {

    fun info(info: CharSequence) {
        msg.printMessage(Diagnostic.Kind.NOTE, info)
    }

    fun error(error: CharSequence) {
        msg.printMessage(Diagnostic.Kind.ERROR, "[$error]")
    }

    fun error(error: Throwable?) {
        msg.printMessage(Diagnostic.Kind.ERROR, "[${error?.message}] \n${formatStackTrace(error?.stackTrace ?: arrayOf())}")
    }

    fun warning(warning: CharSequence) {
        msg.printMessage(Diagnostic.Kind.WARNING, warning)
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }
}