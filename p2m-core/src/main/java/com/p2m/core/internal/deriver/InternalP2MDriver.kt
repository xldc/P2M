package com.p2m.core.internal.deriver

import android.os.Looper
import com.p2m.core.driver.P2MDriver
import com.p2m.core.driver.P2MDriverState
import com.p2m.core.internal.module.AppModuleInit
import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.log.logW
import com.p2m.core.internal.module.ModuleGraphExecution
import com.p2m.core.internal.module.ModuleGraph

internal object InternalP2MDriver : P2MDriver, P2MDriverState {

    internal lateinit var builder: InternalP2MDriverBuilder
    override var opened: Boolean = false
    override var opening: Boolean = false

    override fun open() {
        if (opened) {
            logW("P2M has been driven.")
            return
        }

        check(Looper.getMainLooper().thread === Thread.currentThread()) {
            "Calling in main thread only."
        }

        logI("Ready to open P2M driver.")
        opening = true
        openReal()
        opening = false
        opened = true
        logI("P2M driver open success.")
    }

    private fun openReal() {
        val moduleGraph = ModuleGraph.fromTopModuleInit(AppModuleInit(builder.context, builder.evaluateListener, builder.executeListener))
        val moduleGraphExecution = ModuleGraphExecution(moduleGraph, builder.innerModuleManager)
        moduleGraphExecution.runningAndLoop(BeginDirection.TAIL)
    }

}