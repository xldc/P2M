package com.p2m.core.internal.module

import com.p2m.core.module.Module

/**
 * Module visitor.
 */
internal interface ModuleVisitor {
    fun visit(module: Module<*>)
}