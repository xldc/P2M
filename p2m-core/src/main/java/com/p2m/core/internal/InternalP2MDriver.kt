package com.p2m.core.internal

import android.os.Looper
import androidx.lifecycle.mutable.NoStickyBackgroundLiveEvent
import com.p2m.core.P2MDriver
import com.p2m.core.P2MDriverState
import com.p2m.core.event.P2MMutableBackgroundLiveEvent
import com.p2m.core.event.mutable.P2MNoStickyBackgroundLiveEvent
import com.p2m.core.internal.module.AppModule
import com.p2m.core.internal.execution.BeginDirection
import com.p2m.core.internal.log.logI
import com.p2m.core.internal.log.logW
import com.p2m.core.internal.module.ModuleGraphExecution
import com.p2m.core.internal.module.ModuleGraph

object InternalP2MDriver :P2MDriver, P2MDriverState {

    internal lateinit var builder: P2MDriver.Builder
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

        logW("Ready to open P2M driver.")
        opening = true
        openReal()
        opening = false
        opened = true
        logI("P2M driver open success.")
    }

    private fun openReal() {
        val moduleGraph = ModuleGraph.fromTop(AppModule(builder.context, builder.evaluateListener, builder.executeListener))
        val moduleGraphExecution = ModuleGraphExecution(moduleGraph, builder.innerModuleManager)
        moduleGraphExecution.runningAndLoop(BeginDirection.TAIL)
    }

}