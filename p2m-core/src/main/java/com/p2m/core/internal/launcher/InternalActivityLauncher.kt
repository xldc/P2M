package com.p2m.core.internal.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.core.launcher.*

internal class InternalActivityLauncher<I, O>(
    private val clazz: Class<*>,
    private val createActivityResultContractBlock: () -> ActivityResultContractP2MCompact<I, O>
) : ActivityLauncher<I, O> {

    /**
     * Create a instance of Intent for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type) are null, though they can be modified
     * later with explicit calls.
     *
     * @return a instance of Intent.
     */
    private fun createIntent(): Intent {
        return InternalSafeIntent(clazz)
    }

    override fun launch(context: Context, onIntercept : OnActivityLaunchIntercept?, onFillIntent: OnFillIntent?) {
        context.startActivity(createIntent().also {
            onFillIntent?.invoke(it)
        })
    }

    override fun launch(activity: Activity, onIntercept : OnActivityLaunchIntercept?, onFillIntent: OnFillIntent?) {
        activity.startActivity(createIntent().also {
            onFillIntent?.invoke(it)
        })
    }

    override fun launch(fragment: Fragment, onIntercept : OnActivityLaunchIntercept?, onFillIntent: OnFillIntent?) {
        fragment.startActivity(createIntent().also {
            onFillIntent?.invoke(it)
        })
    }

    override fun registerForActivityResult(activity: ComponentActivity, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O> {
        return activity.registerForActivityResult(createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }.compact()
    }

    override fun registerForActivityResult(fragment: Fragment, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O> {
        return fragment.registerForActivityResult(createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }.compact()
    }

    override fun registerForActivityResult(activityResultRegistry: ActivityResultRegistry, key: String, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncherP2MCompact<I, O> {
        return activityResultRegistry.register(key, createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }.compact()
    }

    private fun createActivityResultContract(): ActivityResultContractP2MCompact<I, O> {
        return createActivityResultContractBlock.invoke().also { it.activityClazz = clazz }
    }

    private fun ActivityResultLauncher<I>.compact(): ActivityResultLauncherP2MCompact<I, O> =
        ActivityResultLauncherP2MCompact(this@InternalActivityLauncher, this)

}