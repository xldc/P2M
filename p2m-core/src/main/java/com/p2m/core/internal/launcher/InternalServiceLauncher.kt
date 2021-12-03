package com.p2m.core.internal.launcher

import com.p2m.core.launcher.LaunchServiceBlock
import com.p2m.core.launcher.ServiceLauncher

internal class InternalServiceLauncher(private val clazz: Class<*>) : ServiceLauncher {
    override fun launchCalling(launchBlock: LaunchServiceBlock) {
        InternalSafeIntent(clazz).apply(launchBlock)
    }
}