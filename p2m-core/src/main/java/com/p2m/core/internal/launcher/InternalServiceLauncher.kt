package com.p2m.core.internal.launcher

import com.p2m.core.launcher.InternalLaunchChannel
import com.p2m.core.launcher.LaunchServiceBlock
import com.p2m.core.launcher.ServiceLauncher

internal class InternalServiceLauncher(private val clazz: Class<*>) : ServiceLauncher {
    override fun launchBlock(launchBlock: LaunchServiceBlock) = InternalLaunchChannel.newBuilder(this)
            .build()

}