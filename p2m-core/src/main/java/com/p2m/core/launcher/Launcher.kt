package com.p2m.core.launcher

interface Launcher

class LaunchChannelDelegate(launcher: Launcher, channel: Channel) : LaunchChannel(launcher, channel) {
    companion object {
        fun create(launcher: Launcher, channel: Channel) =
            LaunchChannelDelegate(launcher, channel)
    }


}

abstract class LaunchChannel(val launcher: Launcher, channel: Channel): InterceptableChannel(launcher, channel)

val Launcher.isActivityLauncher: Boolean
    inline get() = this is ActivityLauncher<*, *>

inline val Launcher.isFragmentLauncher: Boolean
    get() = this is FragmentLauncher<*>

inline val Launcher.isServiceLauncher: Boolean
    get() = this is ServiceLauncher