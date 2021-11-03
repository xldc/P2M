package com.p2m.core.internal.launcher

import android.content.Context
import android.content.Intent
import com.p2m.core.launcher.ActivityLauncher

internal class InternalActivityLauncher(private val clazz: Class<*>) : ActivityLauncher {
    override fun createIntent(context: Context): Intent {
        return Intent(context, clazz)
    }
}