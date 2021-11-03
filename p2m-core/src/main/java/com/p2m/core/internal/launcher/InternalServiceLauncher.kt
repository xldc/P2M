package com.p2m.core.internal.launcher

import android.content.Context
import android.content.Intent
import com.p2m.core.launcher.ActivityLauncher
import com.p2m.core.launcher.ServiceLauncher

internal class InternalServiceLauncher(private val clazz: Class<*>) : ServiceLauncher {
    override fun createIntent(context: Context): Intent {
        return Intent(context, clazz)
    }
}