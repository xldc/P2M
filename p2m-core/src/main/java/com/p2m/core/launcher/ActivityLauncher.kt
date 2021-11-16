package com.p2m.core.launcher

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import com.p2m.annotation.module.api.ApiLauncher
import com.p2m.annotation.module.api.ApiLauncherActivityResultContractFor
import com.p2m.core.internal.launcher.InternalActivityLauncher
import com.p2m.core.internal.launcher.InternalSafeIntent
import kotlin.reflect.KProperty

/**
 * A launcher of Activity.
 *
 * @see ApiLauncher
 */
interface ActivityLauncher<I, O> {

    class Delegate<I, O>(clazz: Class<*>, createActivityResultContractBlock: () -> ActivityResultContractP2MCompact<I, O>) {
        private val real by lazy(LazyThreadSafetyMode.NONE) {
            InternalActivityLauncher<I, O>(clazz, createActivityResultContractBlock)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityLauncher<I, O> = real
    }

    /**
     * Create a instance of Intent for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with explicit calls.
     *
     * @return a instance of Intent.
     */
    fun createIntent(context: Context): Intent

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(context: Context, onFillIntent: OnFillIntent? = null)

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(activity: Activity, onFillIntent: OnFillIntent? = null)

    /**
     * Launch for that [Activity] class annotated by [ApiLauncher],
     * all other fields (action, data, type, class) are null, though they can be modified
     * later with [onFillIntent] calls.
     */
    fun launch(fragment: Fragment, onFillIntent: OnFillIntent? = null)

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * Can use [ApiLauncherActivityResultContractFor] to annotated a implement class of
     * [ActivityResultContractP2MCompact] for the activity, and that implement class must has
     * empty constructor for internal create.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(activity: ComponentActivity, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * Can use [ApiLauncherActivityResultContractFor] to annotated a implement class of
     * [ActivityResultContractP2MCompact] for the activity, and that implement class must has
     * empty constructor for internal create.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(fragment: Fragment, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

    /**
     * Register a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * Can use [ApiLauncherActivityResultContractFor] to annotated a implement class of
     * [ActivityResultContractP2MCompact] for the activity, and that implement class must has
     * empty constructor for internal create.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun registerForActivityResult(activityResultRegistry: ActivityResultRegistry, key: String, callback: ActivityResultCallbackP2MCompact<O>): ActivityResultLauncher<I>

    /**
     * Launch a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * Can use [ApiLauncherActivityResultContractFor] to annotated a implement class of
     * [ActivityResultContractP2MCompact] for the activity, and that implement class must has
     * empty constructor for internal create.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun launchForResult(activity: ComponentActivity, input: I, callback: ActivityResultCallbackP2MCompact<O>)

    /**
     * Launch a activity result for that [Activity] class annotated by [ApiLauncher].
     *
     * Can use [ApiLauncherActivityResultContractFor] to annotated a implement class of
     * [ActivityResultContractP2MCompact] for the activity, and that implement class must has
     * empty constructor for internal create.
     *
     * @return a instance of ActivityResultLauncher.
     *
     * @see ApiLauncher
     * @see ApiLauncherActivityResultContractFor
     * @see ActivityResultContractP2MCompact
     */
    fun launchForResult(fragment: Fragment, input: I, callback: ActivityResultCallbackP2MCompact<O>)
}

abstract class ActivityResultContractP2MCompact<I, O> :
    ActivityResultContract<I, ActivityResultP2MCompact<O>>() {

    internal lateinit var activityClazz: Class<*>

    /**
     *  Input fill to intent.
     */
    abstract fun inputFillToIntent(input: I, intent: Intent)

    final override fun createIntent(context: Context, input: I): Intent {
        val intent = if (input is Intent) InternalSafeIntent(input as Intent) else InternalSafeIntent()
        intent.setComponentInternal(ComponentName(context, activityClazz))
        return intent.also { inputFillToIntent(input, it) }
    }
}

class DefaultActivityResultContractP2MCompact : ActivityResultContractP2MCompact<Intent, Intent>() {

    override fun inputFillToIntent(input: Intent, intent: Intent) = Unit

    override fun parseResult(resultCode: Int, intent: Intent?): ActivityResultP2MCompact<Intent> =
        ActivityResultP2MCompact(resultCode, intent)
}

class ActivityResultP2MCompact<O>(val resultCode: Int, val output: O?)

/**
 * Fill for created intent.
 */
typealias OnFillIntent = Intent.() -> Unit

typealias ActivityResultCallbackP2MCompact<O> = (resultCode: Int, output: O?) -> Unit
