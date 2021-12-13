package com.p2m.core.internal.launcher

import com.p2m.core.launcher.FragmentLauncher
import com.p2m.core.launcher.LaunchFragmentBlock

internal class InternalFragmentLauncher<T>(private val createBlock: () -> T) : FragmentLauncher<T> {
    override fun launch(launchBlock: LaunchFragmentBlock<T>) {
        val launch = {
            launchBlock(createBlock.invoke())
        }

        launch()
    }
}