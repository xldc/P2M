package com.p2m.core.module

interface ModuleCollectorFactory {
    fun newInstance(clazzName: String): ModuleCollector
}