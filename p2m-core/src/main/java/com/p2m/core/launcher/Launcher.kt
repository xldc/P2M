package com.p2m.core.launcher

interface Launcher


val Launcher.isActivityLauncher: Boolean
    inline get() = this is ActivityLauncher<*, *>

inline val Launcher.isFragmentLauncher: Boolean
    get() = this is FragmentLauncher<*>

inline val Launcher.isServiceLauncher: Boolean
    get() = this is ServiceLauncher