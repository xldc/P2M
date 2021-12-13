package com.p2m.core.launcher

import java.lang.Exception

object InterceptorService {
    fun addInterceptor(a: IInterceptor) {

    }

    fun doInterceptions(launchMeta: LaunchMeta, callback: InterceptorCallback) {
        callback.onInterrupt()

        callback.onContinue(launchMeta)
    }

}

/**
 * A callback when launch been intercepted.
 */
interface InterceptorCallback {
    fun onContinue(launchMeta: LaunchMeta)

    fun onInterrupt(e: Exception? = null)
}

interface IInterceptor {
    fun process(launchMeta: LaunchMeta, callback: InterceptorCallback)
}

/**
 * A callback when launch been intercepted.
 */
interface OnIntercept {
    fun onIntercept()
}


interface LaunchInvoker {

    fun launch()

    fun onIntercept(block: () -> Unit): LaunchInvoker

    fun onFailure(block: (e: Exception) -> Unit): LaunchInvoker
}

interface LaunchMeta {
    val launcher: Launcher
    val isGreenChannel: Boolean
}

internal class InternalLaunchMeta private constructor(builder: Builder) : LaunchMeta, LaunchInvoker {
    companion object {
        internal fun newBuilder(launcher: Launcher, isGreenChannel: Boolean = false) = Builder(launcher, isGreenChannel)
    }

    override val launcher = builder.launcher
    override val isGreenChannel: Boolean = builder.isGreenChannel
    private val launchBlock = builder.launchBlock

    override fun launch() {
        val realLaunch = {
            launchBlock()
        }

        if (isGreenChannel) {
            realLaunch()
            return
        }

        InterceptorService.doInterceptions(this, object : InterceptorCallback {
            override fun onContinue(launchMeta: LaunchMeta) {
                realLaunch()
            }

            override fun onInterrupt(e: Exception?) {

            }

        })
    }

    override fun onIntercept(block: () -> Unit): LaunchInvoker {
        return this
    }

    override fun onFailure(block: (e: Exception) -> Unit): LaunchInvoker {
        return this
    }

    internal class Builder(val launcher: Launcher, val isGreenChannel: Boolean) {
        lateinit var launchBlock: () -> Unit

        fun launchBlock(block: () -> Unit): Builder {
            this.launchBlock = block
            return this
        }

        fun build() = InternalLaunchMeta(this)
    }
}

interface ReLauncher {
    fun getLauncher(): Launcher

    fun invoke()
}

internal class InternalRelaunch(builder: Builder) : ReLauncher {

    companion object {
        fun newBuilder(launcher: Launcher) = Builder(launcher)
    }

    private var relaunch: Recall = builder.relaunch
    private var launcher: Launcher = builder.launcher

    override fun getLauncher(): Launcher = launcher

    override fun invoke() = relaunch.invoke()

    class Builder(val launcher: Launcher) {
        lateinit var relaunch: Recall

        fun launchBlock(block: () -> Unit): Builder {
            this.relaunch = InternalRecall(block)
            return this
        }

        fun build() = InternalRelaunch(this)
    }
}

interface Recall {
    fun invoke()
}

internal class InternalRecall(private val block: () -> Unit) : Recall {

    override fun invoke() {
        block()
    }
}