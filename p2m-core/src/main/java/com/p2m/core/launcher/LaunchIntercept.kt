package com.p2m.core.launcher

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

class LaunchInterceptorManager {
    fun addInterceptor(a:LaunchInterceptor){

    }
}

/**
 * A callback when launch been intercepted.
 */
interface LaunchInterceptor {
    fun onLaunch(relaunchCaller: RelaunchCaller): Boolean
}

/**
 * A callback when launch been intercepted.
 */
interface OnLaunchIntercept {
    fun onIntercept()
}

interface RelaunchCaller{
    fun getLauncher(): Launcher

    fun relaunch()
}

enum class LaunchFunctionType{
    SERVICE,
    FRAGMENT,
    ACTIVITY_BY_ACTIVITY,
    ACTIVITY_BY_FRAGMENT,
    ACTIVITY_BY_CONTEXT,
    ACTIVITY_RESULT,
}


internal class InternalRelaunchCaller(builder: Builder) : RelaunchCaller {

    companion object {
        fun builder(launcher: Launcher, type: LaunchFunctionType) =
            Builder(launcher, type)
    }

    private var type: LaunchFunctionType = builder.type
    private var launcher: Launcher = builder.launcher
    private var params: Map<String, Any?> = builder.params

    override fun getLauncher(): Launcher = launcher

    @Suppress("UNCHECKED_CAST")
    override fun relaunch() {
        when (type) {
            LaunchFunctionType.SERVICE -> {

            }
            LaunchFunctionType.FRAGMENT -> {

            }
            LaunchFunctionType.ACTIVITY_BY_ACTIVITY -> {
                val launcher = launcher as ActivityLauncher<*, *>
                launcher.launch(
                    activity = params["activity"] as Activity,
                    onIntercept = params["onIntercept"] as? OnLaunchIntercept,
                    onFillIntent = params["onFillIntent"] as? OnFillIntent
                )
            }
            LaunchFunctionType.ACTIVITY_BY_FRAGMENT -> {
                val launcher = launcher as ActivityLauncher<*, *>
                launcher.launch(
                    fragment = params["fragment"] as Fragment,
                    onIntercept = params["onIntercept"] as? OnLaunchIntercept,
                    onFillIntent = params["onFillIntent"] as? OnFillIntent
                )
            }
            LaunchFunctionType.ACTIVITY_BY_CONTEXT -> {
                val launcher = launcher as ActivityLauncher<*, *>
                launcher.launch(
                    context = params["context"] as Context,
                    onIntercept = params["onIntercept"] as? OnLaunchIntercept,
                    onFillIntent = params["onFillIntent"] as? OnFillIntent
                )
            }
            LaunchFunctionType.ACTIVITY_RESULT -> {
                val launcher = launcher as ActivityLauncher<*, *>
                launcher.launch(
                    activity = params["activity"] as Activity,
                    onIntercept = params["onIntercept"] as? OnLaunchIntercept,
                    onFillIntent = params["onFillIntent"] as? OnFillIntent
                )
            }
        }
    }

    class Builder(val launcher: Launcher, val type: LaunchFunctionType) {
        val params = mutableMapOf<String, Any?>()

        fun addParam(name: String, value: Any?) {
            params[name] = value
        }

        fun build() = InternalRelaunchCaller(this)
    }
}