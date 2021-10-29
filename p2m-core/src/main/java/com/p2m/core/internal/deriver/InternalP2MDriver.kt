package com.p2m.core.internal.deriver

import android.os.Looper
import com.p2m.core.driver.P2MDriver
import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.log.logW
import com.p2m.core.internal.module.ModuleGraph
import com.p2m.core.internal.module.ModuleGraphExecution

internal class InternalP2MDriver(private val builder: InternalP2MDriverBuilder) : P2MDriver{

    override fun open() {
        if (builder.driverState.opened) {
            logW("P2M has been driven.")
            return
        }

        check(Looper.getMainLooper().thread === Thread.currentThread()) {
            "Calling in main thread only."
        }

        logI("Ready to open P2M driver.")
        builder.driverState.opening = true
        openReal()
        builder.driverState.opening = false
        builder.driverState.opened = true
        logI("P2M driver open success.")
    }

    private fun openReal() {
        val moduleGraph = ModuleGraph.create(builder.context, builder.moduleContainer)
        val moduleGraphExecution = ModuleGraphExecution(moduleGraph, builder.evaluateListener, builder.executeListener)
        moduleGraphExecution.runningAndLoop(BeginDirection.TAIL)
    }

}