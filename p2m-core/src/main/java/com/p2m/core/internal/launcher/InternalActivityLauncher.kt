package com.p2m.core.internal.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import com.p2m.core.launcher.ActivityLauncher
import com.p2m.core.launcher.ActivityResultCallbackP2MCompact
import com.p2m.core.launcher.ActivityResultContractP2MCompact
import com.p2m.core.launcher.OnFillIntent

internal class InternalActivityLauncher<I, O>(
    private val clazz: Class<*>,
    private val createActivityResultContractBlock: () -> ActivityResultContractP2MCompact<I, O>
) : ActivityLauncher<I, O> {
    override fun createIntent(context: Context): Intent {
        return InternalSafeIntent(context, clazz)
    }

    override fun launch(context: Context, onFillIntent: OnFillIntent?) {
        context.startActivity(createIntent(context).also {
            onFillIntent?.invoke(it)
        })
    }

    override fun launch(activity: Activity, onFillIntent: OnFillIntent?) {
        activity.startActivity(createIntent(activity).also {
            onFillIntent?.invoke(it)
        })
    }

    override fun launch(fragment: Fragment, onFillIntent: OnFillIntent?) {
        fragment.startActivity(createIntent(fragment.requireContext()).also {
            onFillIntent?.invoke(it)
        })
    }

    override fun registerForActivityResult(activity: ComponentActivity, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I> {
        return activity.registerForActivityResult(createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }
    }

    override fun registerForActivityResult(fragment: Fragment, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I> {
        return fragment.registerForActivityResult(createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }
    }

    override fun registerForActivityResult(activityResultRegistry: ActivityResultRegistry, key: String, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I> {
        return activityResultRegistry.register(key, createActivityResultContract()) {
            callback.invoke(it.resultCode, it.output)
        }
    }

    override fun launchForResult(activity: ComponentActivity, input: I, callback: ActivityResultCallbackP2MCompact<O>) {
        registerForActivityResult(activity, callback).launch(input)
    }

    override fun launchForResult(fragment: Fragment, input: I, callback: ActivityResultCallbackP2MCompact<O>) {
        registerForActivityResult(fragment, callback).launch(input)
    }

    private fun createActivityResultContract(): ActivityResultContractP2MCompact<I, O> {
        return createActivityResultContractBlock.invoke().also { it.activityClazz = clazz }
    }
}